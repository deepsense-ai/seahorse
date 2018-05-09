define([
    'base/js/namespace',
    'base/js/events'
    ],
    function(IPython, events) {
        events.on('kernel_connected.Kernel',
            function () {
                IPython.notebook.set_autosave_interval(5000); //in milliseconds
            }
        );
    }
);
