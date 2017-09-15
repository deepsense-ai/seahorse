'use strict';

import moment from 'moment';
import uploadWorkflowTpl from '../common/modals/upload-workflow-modal/upload-workflow-modal.html';
import newWorkflowTpl from '../common/modals/new-workflow-modal/new-workflow-modal.html';

/* @ngInject */
function Home($rootScope, $uibModal, $state, WorkflowService, ConfirmationModalService, SessionManagerApi,
              WorkflowCloneService, ServerCommunication, SessionManager, config, UserService, PredefinedUser) {
  this.init = () => {
    $rootScope.stateData.dataIsLoaded = true;
    $rootScope.pageTitle = 'Workflows';
    ServerCommunication.unsubscribeFromAllExchanges();
    this.workflows = undefined;
    this.sessionManagerState = 'UNDEFINED';
    this.loadingWorkflowsState = 'UNDEFINED';
    this.icon = '';
    this.info = '';
    this.sort = {
      column: 'updated',
      descending: true
    };
    this.predefinedUserId = PredefinedUser.id;
    this.downloadWorkflows();

    SessionManager
      .checkSessionManagerState()
      .then(() => {
        this.sessionManagerState = 'WORKING';
      })
      .catch(() => {
        this.sessionManagerState = 'NOT_WORKING';
      });

    $rootScope.$watch(() => {
      return {
        sessions: SessionManager.sessions,
        workflows: this.workflows
      };
    }, () => {
      _.forEach(this.workflows, (w) => {
        w.sessionStatus = SessionManager.statusForWorkflowId(w.id);
      });
    }, true); // deep: true
  };

  this.downloadWorkflows = () => {
    WorkflowService
      .downloadWorkflows()
      .then((workflows) => {
        this.workflows = workflows;
        this.loadingWorkflowsState = 'LOADED';
      })
      .catch(() => {
        this.loadingWorkflowsState = 'ERROR';
      });
  };

  this.search = (workflow) => {
    let created = moment(workflow.created).format('DD/MM/YYYY - hh:mm a');
    let updated = moment(workflow.updated).format('DD/MM/YYYY - hh:mm a');

    return !this.filterString ||
      workflow.name.toLowerCase().includes(this.filterString.toLowerCase()) ||
      workflow.description.toLowerCase().includes(this.filterString.toLowerCase()) ||
      created.toString().includes(this.filterString.toLowerCase()) ||
      updated.toString().includes(this.filterString.toLowerCase()) ||
      workflow.ownerName.toLowerCase().includes(this.filterString.toLowerCase());
  };

  this.reloadPage = () => {
    window.location.reload();
  };

  this.getTriggerEventBasedOnDescriptionLength = (description) => {
    return description.length > 50 ? 'mouseenter' : 'none';
  };

  this.sortBy = (columnName) => {
    if (this.sort.column === columnName) {
      this.sort.descending = !this.sort.descending;
    } else {
      this.sort.column = columnName;
      this.sort.descending = false;
    }
  };

  this.getCurrentUser = () => {
    return UserService.getSeahorseUser();
  };

  this.isWorkflowOwnedByCurrentUser = (workflow) => {
    return UserService.getSeahorseUser().id === workflow.ownerId;
  };

  this.isLoading = () => {
    return this.sessionManagerState === 'UNDEFINED' ||
      (this.sessionManagerState === 'WORKING' && this.loadingWorkflowsState === 'UNDEFINED');
  };

  this.isReady = () => {
    return this.sessionManagerState === 'WORKING' && this.loadingWorkflowsState === 'LOADED';
  };

  this.isError = () => {
    return this.sessionManagerState === 'NOT_WORKING' || this.loadingWorkflowsState === 'ERROR';
  };

  this.getClass = (columnName) => {
    if (this.sort.column === columnName) {
      let icon = 'glyphicon glyphicon-chevron';
      return this.sort.descending ? icon + '-down' : icon + '-up';
    }
  };

  this.getVersion = () => {
    return config.editorVersion;
  };

  this.goToWorkflowEditor = (workflowId) => {
    $state.go('workflows.editor', {id: workflowId});
  };

  this.cloneWorkflow = (workflow) => {
    WorkflowCloneService.openModal(this.downloadWorkflows, workflow);
  };

  this.deleteWorkflow = function (workflow) {
    ConfirmationModalService.showModal({
      message: 'The operation will delete workflow "' + workflow.name + '". Deletion cannot be undone afterwards.'
    }).then(() => {
      WorkflowService.deleteWorkflow(workflow.id);
      SessionManagerApi.deleteSessionById(workflow.id);
    });
  };

  this.deleteSession = (workflowId) => {
    SessionManagerApi.deleteSessionById(workflowId).then(() => {
      this.downloadWorkflows();
    });
  };

  this.displayCreateWorkflowPopup = (event) => {
    event.preventDefault();

    const modal = $uibModal.open({
      animation: true,
      templateUrl: newWorkflowTpl,
      controller: 'NewWorkflowModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.then((workflowId) => {
      $state.go('workflows.editor', {
        id: workflowId
      });
    });
  };

  this.displayUploadWorkflowPopup = (event) => {
    event.preventDefault();

    const modal = $uibModal.open({
      animation: true,
      templateUrl: uploadWorkflowTpl,
      controller: 'UploadWorkflowModalController as controller',
      backdrop: 'static',
      keyboard: true
    });

    modal.result.then((workflowId) => {
      $state.go('workflows.editor', {
        'id': workflowId
      });
    });
  };

  this.init();
}

exports.function = Home;

exports.inject = function (module) {
  module.controller('Home', Home);
};
