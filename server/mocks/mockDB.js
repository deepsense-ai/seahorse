/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global console*/
'use strict';

/**
 * Creates UI data.
 *
 * @param {object} apiConfig
 */
module.exports = function mockDB(apiConfig) {
  var fs = require('fs'),
      disk = require('sails-disk'),
      Waterline = require('waterline'),
      orm = new Waterline(),
      config = {
        adapters: {
          diskDB: disk
        },
        connections: {
          mockconfig: {
            adapter: 'diskDB'
          }
        }
      };

  orm.loadCollection(Waterline.Collection.extend({
    identity: 'mock',
    connection: 'mockconfig',
    attributes: {
      id: {
        type: 'integer',
        index: true
      },
      version: {
        type: 'string'
      }
    }
  }));

  orm.initialize(config, function(error) {
    if (error) {
      console.error('error while initializing mock config', error);
      return;
    }

    orm.collections.mock.findOne().where({id: 1}).then((result) => {
      if (!result) {
        orm .collections.mock.create({id: 1, version: 1}).then((result) => {
          console.log('creating mock UI data');
          let experimentModel = apiConfig.localDB.collections.experiment;
          fs.readFile(__dirname + '/fixtures/experiments.json', 'utf-8', (error, data) => {
            if (error) {
              console.error('error while reading experiments list file', error);
              return;
            }
            data = JSON.parse(data);
            for (let i = 0, k = data.experiments.length; i < k; i++) {
              let id = data.experiments[i].id;
              experimentModel.create({id: id}).then((result) => {
                fs.readFile(__dirname + '/fixtures/experiments.'+ id +'.json', 'utf-8',
                  (error, item) => {
                    if (error) {
                      console.error('error while reading experiment data file', error);
                      return;
                    }
                    item = JSON.parse(item);
                    let uiData = {
                      nodes: {}
                    };
                    let graphNodes = item.experiment.graph.nodes;
                    for (let i = graphNodes.length - 1; i >= 0; i--) {
                      uiData.nodes[graphNodes[i].id] = {
                        x: 100 + 50 * Math.floor(Math.random() * 10),
                        y: 100 + 100 * Math.floor(Math.random() * 10),
                      };
                    }
                    experimentModel.update().where({id: id}).set(uiData).catch((error) => {
                      console.error('error while saving experiment mock data', error);
                    });
                  }
                );
              }).catch((error) => {
                console.error('error while saving experiment mock data', error);
              });
            }
          });
        }).catch((error) => {
          console.error('error while saving mock config version', error);
        });
      }
    }).catch((error) => {
      console.error('error while reading mock config version', error);
    });
  });
};
