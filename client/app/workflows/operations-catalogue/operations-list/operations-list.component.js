import operationsListTemplate from './operations-list.html';

import './operations-list.less';

const TYPE_CATEGORY = 'category';
const TYPE_ITEM = 'item';

const ELEMENT_WIDTH = 200;
const ELEMENT_BORDER = 2;
const ELEMENT_HEIGHT = 40;

const MAX_VISIBLE_ELEMENTS = 6;

const DIRECTION_RIGHT = 'right';
const DIRECTION_LEFT = 'left';

const OperationsListComponent = {
  bindings: {
    items: '<',
    containment: '<',
    direction: '<',
    selectOperation: '&'
  },
  controller: class OperationsListController {
    constructor(CanvasService, $element) {
      'ngInject';

      this.$element = $element;
      this.CanvasService = CanvasService;
    }

    getNextLevelStyle() {
      return {
        left: this.nextLevelLeft + 'px',
        top: this.nextLevelTop + 'px'
      };
    }

    calculateNextLevelPosition(event, nextLevelItems) {
      // Get only the container element from event
      const element = event.target;

      const elementBoundingRect = element.getBoundingClientRect();
      const containmentBoundingRect = this.containment[0].getBoundingClientRect();

      // Calculate bounds
      const containerBottom = containmentBoundingRect.bottom;
      const containerRight = containmentBoundingRect.right;
      const containerLeft = containmentBoundingRect.left;

      //C alculate starting points for nextLevel menu
      const elementTop = elementBoundingRect.top;
      const elementLeft = elementBoundingRect.left;
      const elementRight = elementBoundingRect.right;

      // Calculate size of nextLevel menu
      const nextLevelVisibleElements = Math.min(nextLevelItems, MAX_VISIBLE_ELEMENTS);
      const nextLevelHeight = nextLevelVisibleElements * ELEMENT_HEIGHT * this.CanvasService.scale;
      const nextLevelWidth = ELEMENT_WIDTH * this.CanvasService.scale;

      // Adjust initial starting position to the origin element position
      this.nextLevelTop = element.offsetTop - element.parentNode.scrollTop - ELEMENT_BORDER;
      // Move up, if next level menu wont fit below
      if (elementTop + nextLevelHeight > containerBottom) {
        this.nextLevelTop -= (nextLevelVisibleElements - 1) * ELEMENT_HEIGHT;
      }

      if (this.direction === DIRECTION_LEFT) {
        if (elementLeft - nextLevelWidth < containerLeft) {
          this.nextLevelLeft = ELEMENT_WIDTH - 2 * ELEMENT_BORDER;
          // Change direction for the next level menu to expand in the same direction
          this.direction = DIRECTION_RIGHT;
        } else {
          this.nextLevelLeft = -ELEMENT_WIDTH;
        }
      } else if (elementRight + nextLevelWidth < containerRight) {
          this.nextLevelLeft = ELEMENT_WIDTH - 2 * ELEMENT_BORDER;
        } else {
          this.nextLevelLeft = -ELEMENT_WIDTH;
          // Change direction for the next level menu to expand in the same direction
          this.direction = DIRECTION_LEFT;
        }
    }

    select(operation) {
      if (operation.type === TYPE_ITEM) {
        this.selectOperation({operationId: operation.id});
      }
    }

    showItem(event, index) {
      if (this.items[index].type !== TYPE_CATEGORY) {
        this.selectedItems = null;
        return;
      }
      const catalog = this.items[index].catalog.map((category) => {
        return Object.assign({}, category, {type: TYPE_CATEGORY});
      });
      const items = this.items[index].items.map((item) => {
        return Object.assign({}, item, {type: TYPE_ITEM});
      });
      this.selectedItems = [...catalog, ...items];
      this.calculateNextLevelPosition(event, this.selectedItems.length);
    }

    $onChanges(change) {
      if (change.items) {
        this.selectedItems = null;
      }
    }

    $postLink() {
      // Prevent canvas wheel zooming
      $(this.$element[0]).on('wheel', (e) => {
        e.stopPropagation();
      });

      // Mousedown is used to drag element around - and it prevents using scrollbar on FF
      $(this.$element[0]).on('mousedown', (e) => {
        e.stopPropagation();
      });
    }

    $destroy() {
      $(this.$element[0]).off();
    }
  },
  templateUrl: operationsListTemplate
};

export default OperationsListComponent;
