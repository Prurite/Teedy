/** AI-genereated-content
 *  Tool: Github Copilot
 *  Version: Claude 3.7 Sonnet
 *  Usage: [Prompt:]
 *    Create necessary controllers and views for the admin to view and approve / decline new user registration requests.
 *    [zh_CN.json] [en.json] [UserRegistrationRequestResource.java] [Login.js] [settings.user.html]
 */

'use strict';

/**
 * Settings registration requests controller.
 */
angular.module('docs').controller('SettingsUserRequest', function($scope, $state, Restangular, $uibModal) {
  /**
   * Load registration requests from server.
   */
  $scope.loadRequests = function() {
    Restangular.one('user/registration/list').get({
      sort_column: 3, // sort by creation date
      asc: false      // newest first
    }).then(function(data) {
      $scope.requests = data.requests;
    });
  };
  
  // Initialize the page
  $scope.loadRequests();
  
  /**
   * Open the approval modal for a registration request.
   */
  $scope.approve = function(request) {
    // Reset any previous errors
    $scope.error = null;
    
    $scope.selectedRequest = request;
    $scope.user = {
      username: '',
      password: '',
      storage_quota: 1000 // Default quota, 1GB in MB
    };
    
    // Store the modal instance so we can close it programmatically
    $scope.modalInstance = $uibModal.open({
      templateUrl: 'approveModalTemplate',
      scope: $scope,
      backdrop: 'static',
      size: 'md'
    });
  };
  
  /**
   * Approve a registration request and create the user.
   */
  $scope.confirmApprove = function() {
    // Clear previous errors
    $scope.error = null;
    
    Restangular.one('user/registration', $scope.selectedRequest.id).post('approve', {
      username: $scope.user.username,
      password: $scope.user.password,
      storage_quota: $scope.user.storage_quota
    }).then(function() {
      // Close the modal after success
      $scope.modalInstance.close();
      
      // Refresh the requests list
      $scope.loadRequests();
    }, function(e) {
      // Display errors in the modal
      if (e.data && e.data.type) {
        switch (e.data.type) {
          case 'ValidationError':
            $scope.error = 'Please check all fields are filled correctly';
            break;
          case 'AlreadyExistingUsername':
            $scope.error = 'This username is already taken by another user';
            break;
          default:
            $scope.error = 'Server error: ' + (e.data.message || 'Unknown error');
        }
      } else {
        $scope.error = 'An unknown error occurred';
      }
    });
  };
  
  /**
   * Reject a registration request.
   */
  $scope.reject = function(request) {
    var confirmModal = $uibModal.open({
      template: '<div class="modal-header">' +
                '<h3 class="modal-title" translate="settings.request.reject_title"></h3>' +
                '</div>' +
                '<div class="modal-body">' +
                '<p translate="settings.request.confirm_reject"></p>' +
                '</div>' +
                '<div class="modal-footer">' +
                '<button class="btn btn-default" ng-click="$dismiss()" translate="cancel"></button>' +
                '<button class="btn btn-danger" ng-click="$close(true)" translate="ok"></button>' +
                '</div>',
      backdrop: 'static',
      size: 'sm'
    });
    
    confirmModal.result.then(function() {
      Restangular.one('user/registration', request.id).post('reject', {}).then(function() {
        // Refresh the requests list
        $scope.loadRequests();
      }, function(e) {
        // Display error notification
        $scope.error = 'An error occurred while rejecting the request';
      });
    });
  };
});