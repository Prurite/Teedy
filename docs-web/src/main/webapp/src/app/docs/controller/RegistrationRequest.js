/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *    Create necessary controllers and views for the user to submit a registration request.
 *    [zh_CN.json] [en.json] [UserRegistrationRequestResource.java] [Login.js] [login.html]
 */

'use strict';

/**
 * Registration Request controller.
 */
angular.module('docs').controller('RegistrationRequest', function($scope, $rootScope, $state, Restangular) {
  $scope.request = {
    name: '',
    email: ''
  };
  
  $scope.success = false;
  $scope.error = null;
  $scope.submitting = false;
  
  /**
   * Submit the registration request.
   */
  $scope.submit = function() {
    $scope.submitting = true;
    $scope.error = null;
    
    Restangular.one('user/registration').put($scope.request)
      .then(function() {
        $scope.success = true;
        $scope.submitting = false;
      }, function(response) {
        $scope.submitting = false;
        if (response.status === 400) {
          if (response.data.type === 'AlreadyRequested') {
            $scope.error = 'registration.already_requested';
          } else if (response.data.type === 'EmailExists') {
            $scope.error = 'registration.email_exists';
          } else {
            $scope.error = 'validation.server';
          }
        } else if (response.status === 403) {
          $scope.error = 'permission.denied';
        } else {
          $scope.error = 'validation.server';
        }
      });
  };
  
  /**
   * Cancel and return to login page.
   */
  $scope.cancel = function() {
    $state.go('login');
  };
});