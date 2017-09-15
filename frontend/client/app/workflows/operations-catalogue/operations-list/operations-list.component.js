/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import operationsListTemplate from './operations-list.html';

import './operations-list.less';

const TYPE_CATEGORY = 'category';
const TYPE_ITEM = 'item';

const ELEMENT_WIDTH = 200;
const ELEMENT_WIDTH_SMALLER = 180;
const ELEMENT_BORDER = 2;
const CATEGORY_HEIGHT = 40;

const ITEM_HEIGHT = 25;
const ITEM_MARGIN_TOP = 10;
const ITEM_MARGIN_BOTTOM = 5;

const CATALOG_MAX_HEIGHT = 240;

const MAX_VISIBLE_ELEMENTS = 6;

const DIRECTION_RIGHT = 'right';
const DIRECTION_LEFT = 'left';

const OperationsListComponent = {
  bindings: {
    containment: '<',
    level: '<',
    direction: '<',
    items: '<',
    selectOperation: '&'
  },
  controller: class OperationsListController {
    constructor(CanvasService, OperationsCatalogueService, $element, $scope) {
      'ngInject';

      this.currentLevel = 1;
      this.visibleLevel = 1;
      this.activeCategoryIndex = null;

      this.$element = $element;
      this.CanvasService = CanvasService;
      this.OperationsCatalogueService = OperationsCatalogueService;

      $scope.$watch(() => OperationsCatalogueService.getVisibleCatalogueLevel(), (level) => {
        this.visibleLevel = level;
      });
    }

    $onChanges(change) {
      if (change.items) {
        this.selectedItems = null;
        this.activeCategoryIndex = null;
      }

      if (change.level && change.level.currentValue) {
        this.currentLevel = change.level.currentValue + 1;
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

    $onDestroy() {
      $(this.$element[0]).off();
      this.OperationsCatalogueService.setVisibleCatalogueLevel(1);
    }

    getNextLevelStyle() {
      return {
        left: this.nextLevelLeft + 'px',
        top: this.nextLevelTop + 'px'
      };
    }

    calculateNextLevelPosition(event, catalogLength, itemsLength) {
      // Get only the container element from event
      const element = event.target;

      const elementBoundingRect = element.getBoundingClientRect();
      const containmentBoundingRect = this.containment[0].getBoundingClientRect();

      const nextElementWidth = (this.currentLevel > 1) ? ELEMENT_WIDTH_SMALLER : ELEMENT_WIDTH;
      const currentElementWidth = (this.currentLevel > 2) ? ELEMENT_WIDTH_SMALLER : ELEMENT_WIDTH;

      // Calculate bounds
      const containerBottom = containmentBoundingRect.bottom;
      const containerRight = containmentBoundingRect.right;
      const containerLeft = containmentBoundingRect.left;

      // Calculate starting points for nextLevel menu
      const elementTop = elementBoundingRect.top;
      const elementLeft = elementBoundingRect.left;
      const elementRight = elementBoundingRect.right;

      // Calculate size of nextLevel menu
      const nextLevelCategoryHeight = catalogLength * CATEGORY_HEIGHT;
      const nextLevelItemsMargin = (itemsLength) ? ITEM_MARGIN_BOTTOM + ITEM_MARGIN_TOP : 0;
      const nextLevelItemsHeight = itemsLength * ITEM_HEIGHT + nextLevelItemsMargin;
      const nextLevelSize = Math.min((nextLevelCategoryHeight + nextLevelItemsHeight), CATALOG_MAX_HEIGHT);
      const nextLevelHeight = nextLevelSize * this.CanvasService.scale;
      const nextLevelWidth = nextElementWidth * this.CanvasService.scale;

      // Adjust initial starting position to the origin element position
      this.nextLevelTop = element.offsetTop - element.parentNode.scrollTop - ELEMENT_BORDER;
      // Move up, if next level menu wont fit below
      if (elementTop + nextLevelHeight > containerBottom) {
        this.nextLevelTop -= nextLevelSize - CATEGORY_HEIGHT;
      }

      if (this.direction === DIRECTION_LEFT) {
        if (elementLeft - nextLevelWidth < containerLeft) {
          this.nextLevelLeft = currentElementWidth - 2 * ELEMENT_BORDER;
          // Change direction for the next level menu to expand in the same direction
          this.direction = DIRECTION_RIGHT;
        } else {
          this.nextLevelLeft = -nextElementWidth;
        }
      } else if (elementRight + nextLevelWidth < containerRight) {
          this.nextLevelLeft = currentElementWidth - 2 * ELEMENT_BORDER;
        } else {
          this.nextLevelLeft = -nextElementWidth;
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
      this.calculateNextLevelPosition(event, catalog.length, items.length);
      this.OperationsCatalogueService.setVisibleCatalogueLevel(this.currentLevel + 1);
      this.activeCategoryIndex = index;
    }
  },
  templateUrl: operationsListTemplate
};

export default OperationsListComponent;
