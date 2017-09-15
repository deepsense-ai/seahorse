var express = require('express');

module.exports = function(waterline) {
  var router = express.Router();

  var wc = waterline.collections;
  for (collection in wc) {
    if (wc.hasOwnProperty(collection)) {
      var model = wc[collection];
      var response = function(res) {
        return function(err, data) {
          if (err)
            res.send(err);
          res.json(data);
        };
      };

      router.route('/' + collection + '/:id')

        .get(function(req, res) {
          model.findOne({id: req.params.id})
          .exec(response(res));
        })

        .put(function(req, res) {
          model.update({id: req.params.id}, req.body)
          .exec(response(res));
        })

        .delete(function(req, res) {
          model.destroy({id: req.params.id})
          .exec(response(res));
        });

      router.route('/' + collection)

        .get(function(req, res) {
          model.find({})
          .exec(response(res));
        })

        .post(function(req, res) {
          model.create(req.body)
          .exec(response(res));
        });
    }
  }

  return router;
};
