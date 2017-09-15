/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 08.06.15.
 */

angular.module('test', ['deepsense-catalogue-panel'])

.controller('TestCtrl', ['$scope', function ($scope) {
  $scope.collection = [
    {
      "items": [
        {
          "id": "83bad450-f87c-11e4-b939-0800200c9a66",
          "name": "File To DataFrame",
          "$$hashKey": "object:27"
        },
        {
          "id": "748975b2-38f0-40b4-8550-3faf4840b7c5",
          "name": "Read File",
          "$$hashKey": "object:28"
        },
        {
          "id": "2aa22df2-e28b-11e4-8a00-1681e6b88ec1",
          "name": "Load DataFrame",
          "$$hashKey": "object:29"
        },
        {
          "id": "58025c36-e28b-11e4-8a00-1681e6b88ec1",
          "name": "Save DataFrame",
          "$$hashKey": "object:30"
        }
      ],
      "catalog": [],
      "id": "5a39e324-15f4-464c-83a5-2d7fba2858aa",
      "name": "Input/Output",
      "icon": "fa-exchange",
      "$$hashKey": "object:15"
    },
    {
      "items": [
        {
          "id": "ecb9bc36-5f7c-4a62-aa18-8db6e2d73223",
          "name": "Create Mathematical Transformation Over Time",
          "$$hashKey": "object:89"
        },
        {
          "id": "ecb9bc36-5f7c-4a62-aa18-8db6e2d73251",
          "name": "Mathematical Operation",
          "$$hashKey": "object:35"
        },
        {
          "id": "f6e1f59b-d04d-44e2-ae35-2fcada44d23f",
          "name": "Apply Transformation",
          "$$hashKey": "object:36"
        }
      ],
      "catalog": [],
      "id": "3fcc6ce8-11df-433f-8db3-fa1dcc545ed8",
      "name": "Transformation",
      "icon": "fa-bolt",
      "$$hashKey": "object:16"
    },
    {
      "items": [
        {
          "id": "b1b6eefe-f7b7-11e4-a322-1697f925ec7b",
          "name": "One Hot Encoder",
          "$$hashKey": "object:39"
        },
        {
          "id": "06374446-3138-4cf7-9682-f884990f3a60",
          "name": "Join",
          "$$hashKey": "object:40"
        },
        {
          "id": "42f2eb12-e28b-11e4-8a00-1681e6b88ec1",
          "name": "Decompose Datetime",
          "$$hashKey": "object:41"
        },
        {
          "id": "f8b3c5d0-febe-11e4-b939-0800200c9a66",
          "name": "Convert Type",
          "$$hashKey": "object:42"
        },
        {
          "id": "96b28b7f-d54c-40d7-9076-839411793d20",
          "name": "Project Columns",
          "$$hashKey": "object:43"
        },
        {
          "id": "d273c42f-b840-4402-ba6b-18282cc68de3",
          "name": "Split",
          "$$hashKey": "object:44"
        }
      ],
      "catalog": [],
      "id": "6c730c11-9708-4a84-9dbd-3845903f32ac",
      "name": "Data manipulation",
      "icon": "fa-pencil-square-o",
      "$$hashKey": "object:17"
    },
    {
      "items": [],
      "catalog": [
        {
          "items": [
            {
              "id": "c526714c-e7fb-11e4-b02c-1681e6b88ec1",
              "name": "Train Regressor",
              "$$hashKey": "object:60"
            },
            {
              "id": "6cf6867c-e7fd-11e4-b02c-1681e6b88ec1",
              "name": "Score regressor",
              "$$hashKey": "object:61"
            },
            {
              "id": "0643f308-f2fa-11e4-b9b2-1697f925ec7b",
              "name": "Ridge Regression",
              "$$hashKey": "object:62"
            },
            {
              "id": "95ca5225-b8e0-45c7-8ecd-a2c9d4d6861f",
              "name": "Cross-validate Regressor",
              "$$hashKey": "object:63"
            },
            {
              "id": "f2a43e21-331e-42d3-8c02-7db1da20bc00",
              "name": "Evaluate Regression",
              "$$hashKey": "object:64"
            }
          ],
          "catalog": [],
          "id": "c80397a8-7840-4bdb-83b3-dc12f1f5bc3c",
          "name": "Regression",
          "parentId": "0c730c11-9708-4a84-9dbd-3845903f32ac",
          "icon": "fa-line-chart",
          "$$hashKey": "object:51"
        },
        {
          "items": [
            {
              "id": "ed20e602-ff91-11e4-a322-1697f925ec7b",
              "name": "Logistic Regression",
              "$$hashKey": "object:70"
            },
            {
              "id": "892cf942-fe24-11e4-a322-1697f925ec7b",
              "name": "Train Classifier",
              "$$hashKey": "object:71"
            },
            {
              "id": "6f9a4e9e-fe1a-11e4-a322-1697f925ec7b",
              "name": "Score Classifier",
              "$$hashKey": "object:72"
            },
            {
              "id": "1163bb76-ba65-4471-9632-dfb761d20dfb",
              "name": "Evaluate Classification",
              "$$hashKey": "object:73"
            }
          ],
          "catalog": [],
          "id": "ff13cbbd-f4ec-4df3-b0c3-f6fd4b019edf",
          "name": "Classification",
          "parentId": "0c730c11-9708-4a84-9dbd-3845903f32ac",
          "icon": "fa-tag",
          "$$hashKey": "object:52"
        },
        {
          "items": [
            {
              "id": "c25e034e-ffac-11e4-a322-1697f925ec7b",
              "name": "Select Important Features",
              "$$hashKey": "object:78"
            }
          ],
          "catalog": [],
          "id": "dd29042a-a32c-4948-974f-073c41230da0",
          "name": "Feature selection",
          "parentId": "0c730c11-9708-4a84-9dbd-3845903f32ac",
          "icon": "fa-filter",
          "$$hashKey": "object:53"
        }
      ],
      "id": "0c730c11-9708-4a84-9dbd-3845903f32ac",
      "name": "Machine learning",
      "icon": "fa-square",
      "$$hashKey": "object:18"
    }
  ];
}])

.directive('droppable', function () {
  return {
    link: function (scope, element) {
      element[0].style.color = 'red';
    }
  };
})

.run(function ($timeout) {
    $timeout(function () {
      jQuery(".operations-tree").mCustomScrollbar(
        {
          axis: 'y',
          theme:"dark",
          scrollInertia: 300
        }
      );
    });
});
