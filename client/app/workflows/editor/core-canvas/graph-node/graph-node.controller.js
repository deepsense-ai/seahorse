'use strict';

const CSS_CLASSES_MAP = {
  status_completed: {
    status: 'completed',
    icon: 'fa-check'
  },
  status_running: {
    status: 'running',
    icon: 'fa-cog fa-spin'
  },
  status_queued: {
    status: 'queued',
    icon: 'fa-clock-o'
  },
  status_aborted: {
    status: 'aborted',
    icon: 'fa-exclamation'
  },
  status_failed: {
    status: 'failed',
    icon: 'fa-ban'
  },
  error: {
    status: 'failed',
    icon: 'fa-exclamation'
  }
};

class GraphNodeController {
  constructor($rootScope, $scope, $element, WorkflowService, UserService, nodeTypes, specialOperations, GraphStyleService) {
    'ngInject';
    _.assign(this, {
      $rootScope, $scope, $element, WorkflowService, UserService, nodeTypes, specialOperations,
      GraphStyleService
    });

    this.nodeType = this.getNodeType();
    this.firstNameLetters = this.node.name.split(' ').map((item) => item[0]).join('');

    $scope.$watch(() => this.node.state, (newValue) => {
      if (newValue) {
        this.statusClasses = this.getCssClasses();
      }
    });

    $scope.$watch(() => this.node.knowledgeErrors, (newValue) => {
      var errors = this.node.getFancyKnowledgeErrors();
      this.tooltipMessage = errors ? errors : '';
    });

    this.borderCssClass = this.getBorderColor();
  }

  $postLink() {
    // TODO we don't want to have communication made with $broadcast and events, think about other way
    this.$element.on('click', ($event) => {
      this.$rootScope.$broadcast('GraphNode.CLICK', {
        originalEvent: $event,
        selectedNode: this.node
      });
    });

    this.$element.on('mousedown', ($event) => {
      this.$rootScope.$broadcast('GraphNode.MOUSEDOWN', {
        originalEvent: $event,
        selectedNode: this.node
      });
    });

    this.$element.on('mouseup', ($event) => {
      this.$rootScope.$broadcast('GraphNode.MOUSEUP', {
        originalEvent: $event,
        selectedNode: this.node
      });
    });
  }

  getNodeType() {
    const operationId = this.node.operationId;
    if (operationId in this.specialOperations) {
      return 'action';
    } else if (operationId === this.nodeTypes.CUSTOM_TRANSFORMER_SOURCE || operationId === this.nodeTypes.CUSTOM_TRANSFORMER_SINK) {
      return 'source-or-sink';
    } else {
      return 'standard';
    }
  }

  isOwner() {
    // TODO do wydzielenia do serwisu - sprawdzanie ownera powtarza sie w paru miejsach w aplikacji
    return this.WorkflowService.getCurrentWorkflow().owner.id === this.UserService.getSeahorseUser().id;
  }

  getCssClasses() {
    if (this.node.state.status && this.node.state.status !== 'status_draft') {
      return CSS_CLASSES_MAP[this.node.state.status];
    } else if (this.node.knowledgeErrors.length > 0) {
      return CSS_CLASSES_MAP.error;
    } else {
      return null;
    }
  }

  getBorderColor() {
    let typeQualifier;
    if (this.node.input && this.node.input.length === 1) {
      typeQualifier = this.node.input[0].typeQualifier[0];
    } else if (this.node.originalOutput && this.node.originalOutput.length === 1) {
      typeQualifier = this.node.originalOutput[0].typeQualifier[0];
    }
    const type = this.GraphStyleService.getOutputTypeFromQualifier(typeQualifier);

    return `border-${type}`;
  }

}

export default GraphNodeController;
