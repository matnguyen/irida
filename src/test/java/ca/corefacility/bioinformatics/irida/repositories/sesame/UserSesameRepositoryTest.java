/*
 * Copyright 2013 Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.corefacility.bioinformatics.irida.repositories.sesame;

import ca.corefacility.bioinformatics.irida.repositories.sesame.dao.SailStore;
import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.InvalidPropertyException;
import ca.corefacility.bioinformatics.irida.model.Role;
import ca.corefacility.bioinformatics.irida.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

/**
 *
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
public class UserSesameRepositoryTest {
    
    private UserSesameRepository repo;
    private SailStore store;
    
    @Before
    public void setUp() {
        store = new SailStore();
        store.initialize();
        UserIdentifierGenerator idGen = new UserIdentifierGenerator(store);
        IdentifierGenerator auditIdGen = new IdentifierGenerator(store);
        AuditRepository auditRepo = new AuditRepository(store);
        auditRepo.setIdGen(auditIdGen);
        RelationshipSesameRepository linksRepo = new RelationshipSesameRepository(store, auditRepo);
        repo = new UserSesameRepository(store,auditRepo,linksRepo);
        repo.setIdGen(idGen);
        
        User u = new User("user1", "user1@there", "abc123", "user", "one", "111-111-1111");
        repo.create(u);
        
        u = new User("user2", "user2@there", "bcd234", "user", "two", "222-222-2222");
        repo.create(u);
        
        u = new User("user3", "user3@there", "cde345", "user", "three", "333-333-3333");
        repo.create(u);
        
    }

    /**
     * Test of create method, of class UserSesameRepository.
     */
    @Test
    public void testCreate() {
        User u = new User("test", "testuser@test", "123456", "a", "test", "123-456-7890");

        try {
            u = repo.create(u);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }
    
    @Test
    public void testAddExistingUser() {
        User u = new User("test", "testuser@test", "123456", "a", "test", "123-456-7890");
        User v = new User("test", "testuser@test", "123456", "a", "test", "123-456-7890");
        
        try {
            repo.create(u);
            repo.create(v);
            fail();
        } catch (EntityExistsException e) {
        }
    }
       

    /**
     * Test of getUserByUsername method, of class UserSesameRepository.
     */
    @Test
    public void testGetUserByUsername() {
        User u = new User("test", "testuser@test", "123456", "a", "test", "123-456-7890");

        u = repo.create(u);
        try{
            u = repo.getUserByUsername(u.getUsername());
            assertNotNull(u);
        }
        catch(IllegalArgumentException e){
            fail();
        }
    }
    
    @Test
    public void testGetUserByUsernameInvalid() {
        try{
            User u = repo.getUserByUsername("fake");
            fail();
        }
        catch(EntityNotFoundException e){}
    }


    /**
     * Test of checkUsernameExists method, of class UserSesameRepository.
     */
    @Test
    public void testCheckUsernameExists() {
        User u = new User("test", "testuser@test", "123456", "a", "test", "123-456-7890");
        u = repo.create(u);
        
        try{
            if(! repo.checkUsernameExists(u.getUsername())){
                fail();
            }
        }
        catch(IllegalArgumentException e){
            fail();
        } 
    }
    
    /**
     * Test updating the role of a user
     */
    @Test
    public void testUpdateRole() throws RepositoryException, MalformedQueryException, QueryEvaluationException{
        User u = new User("test", "testuser@test", "123456", "a", "test", "123-456-7890");
        Collection<Role> roles = new ArrayList<>();
        roles.add(new Role("ROLE_USER"));
        roles.add(new Role("ROLE_ADMIN"));
        u.setRoles(roles);
        u = repo.create(u);
        
        roles.add(new Role("ROLE_SUPERUSER"));
        u.setRoles(roles);
        
        HashMap<String,Object> changes = new HashMap<>();
        changes.put("roles", roles);
        
        try{
            User updated = repo.update(u.getIdentifier(),changes);
            assertTrue(updated.getAuthorities().size() == 3);
            
            ObjectConnection con = store.getRepoConnection();
            ValueFactory fac = con.getValueFactory();
            String qs = store.getPrefixes() + 
                    "ASK "
                    + "{?user irida:systemRole ?role }";
            BooleanQuery query = con.prepareBooleanQuery(QueryLanguage.SPARQL, qs);
            query.setBinding("user", fac.createURI(u.getIdentifier().getUri().toString()));
            for(Role r : roles){
                query.setBinding("role", fac.createLiteral(r.getName()));
                boolean res = query.evaluate();
                assertTrue(res);
            }
            
        }
        catch(InvalidPropertyException ex){
            fail(ex.getMessage());
        }
    }

}