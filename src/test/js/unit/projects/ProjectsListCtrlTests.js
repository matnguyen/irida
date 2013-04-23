describe("ProjectsCtrl", function () {
  var newScope, controller, $httpBackend;

  beforeEach(module('irida'));
  beforeEach(inject(function ($rootScope, $controller, $injector) {
    newScope = $rootScope.$new();
    controller = $controller("ProjectsListCtrl", {$scope: newScope});

    $httpBackend = $injector.get('$httpBackend');
    $httpBackend.when('GET', '/projects').respond({projectResources: {projects: [
      {name: 'E. coli', links: [{rel: "self", href: 'http://127.0.0.1:8080/projects/4aee0abd-d6f1-484b-992d-8bf9bd5c7344'}]}
    ]}});
  }));

  afterEach(function () {
    "use strict";
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  it("Should have an empty projects array", function () {
    expect(newScope.projects.length).toBe(0);
  });

//  it("Should have a projects url", function () {
//    expect(newScope.projectsUrl).toContain('/projects?');
//  });
//
//  it("Should make a server call to get the current users", function () {
//    runs(function () {
//      $httpBackend.expectGET('/projects');
//      newScope.loadProjects('/projects');
//      waits(1000);
//      runs(function () {
//        expect(newScope.projects.length).toBe(1);
//      });
//      $httpBackend.flush();
//    });
//  });
});