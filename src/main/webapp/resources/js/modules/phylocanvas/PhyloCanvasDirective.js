/**
 * [PhyloCanvas documentation](http://www.spatialepidemiology.net/PhyloCanvasDocs/)
 * This Directive is a wrapper for the PhyloCanvas Library.
 * 
 * Expected use:
 * <phylocanvas shape="circular" config="{}" newick="[newickString]">
 *   <phylocanvas-controls>
 *     <phylocanvas-control shape="rectangular" text="Rectangular"></phylocanvas-control>
 *     <phylocanvas-control shape="circular" text="Circular"></phylocanvas-control>
 *     <phylocanvas-control shape="radial" text="Radial"></phylocanvas-control>
 *     <phylocanvas-control shape="diagonal" text="Diagonal"></phylocanvas-control>
 *     <phylocanvas-control shape="hierarchy" text="Hierarchical"></phylocanvas-control>
 *   </phylocanvas-controls>
 *   <div id="phylocanvas"></div>
 * </phylocanvas>
 */
(function(angular, PhyloCanvas) {
  "use strict";
  
  angular.module('phylocanvas', [])
  /**
   * PhyloCanvas Directive
   *
   * @param shape: 'rectangular', 'circular', 'radial', 'diagonal', 'hierarchy'
   * @param config: Not required.  Defaults to '{}', see phylocanvas docs.
   */
    .directive('phylocanvas', function phylocanvas() {
      var phylo;
      return {
        restrict  : 'E',
        transclude: true,
        replace   : true,
        template  : '<div ng-transclude></div>',
        scope: {
          shape: "@"
        },
        link: function(scope, element, attrs) {
          angular
            .element("#phylocanvas")
            .css({
              'height': '500px',
              //'width' : '100%'
            });

          phylo = new PhyloCanvas.Tree("phylocanvas", attrs.config);
          phylo.setTreeType(scope.shape);
          phylo.load(attrs.newick);
        },
        controller: ['$scope', function($scope) {
          $scope.setShape = function (shape) {
            phylo.setTreeType(shape);
          };
        }]
      };
    })
  /**
   * Control for the type of visualization.  This is not required.  Only use if you need to dynamically change
   * the type of plot.  Need to use a phylocanvasControl below.
   */
    .directive('phylocanvasControls', function () {
      return {
        require   : '^phylocanvas',
        replace   : true,
        transclude: true,
        template  : '<div style="margin-bottom: 10px;"><div class="btn-group" role="group"><button ng-repeat="control in controls" ng-click="select(control)" class="btn btn-default btn-sm" ng-class="{active: control.selected}">{{control.text}}</button></div><div ng-transclude></div></div>',
        restrict  : "E",
        controller: ['$scope', function ($scope) {
          $scope.controls = [];
          $scope.shape = $scope.$parent.shape;
          $scope.select = function(control) {
            angular.forEach($scope.controls, function (eachControl) {
              eachControl.selected = angular.equals(control, eachControl);
            });
            $scope.$parent.setShape(control.shape);
          };

          this.addControl = function (control) {
            if(control.shape === $scope.shape) {
              control.selected = true;
            }
            $scope.controls.push(control);
          };
        }]
      };
    })
  /**
   * A control for modifying the type of phylocanvas.
   * @param shape - corresponds to the different shapes the plots can be.
   * @param text - text to appear on the button.
   */
    .directive('phylocanvasControl', function () {
      return {
        require : '^phylocanvasControls',
        restrict: 'E',
        replace: true,
        template: '',
        scope   : {
          shape: "@",
          text : "@"
        },
        link    : function (scope, element, attrs, phylocanvasControlsCtrl) {
          phylocanvasControlsCtrl.addControl(scope);
        }
      };
    })
  
    .directive('phylosvg', function() {
      return {
        restrict: 'A',
        replace: false,
        link: function(scope, element, attrs) {
          angular.element(element)
            .css({
              'margin-top': '20px'
            });
          var phylo = new Smits.PhyloCanvas({
            'newick': attrs.newick
          }, 'phyloSVG', 500, 500, 'circular');
        }
      }
    })
  ;
})(window.angular, window.PhyloCanvas);