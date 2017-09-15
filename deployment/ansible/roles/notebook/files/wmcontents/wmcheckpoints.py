# Copyright (c) 2015, CodiLime Inc.

from notebook.services.contents.manager import Checkpoints
import datetime


class WMCheckpoints(Checkpoints):
    def create_checkpoint(self, nb, path):
        return {'id': "stub", 'last_modified': datetime.datetime.now()}

    def restore_checkpoint(self, nb, checkpoint_id, path):
        pass

    def list_checkpoints(self, path):
        return []

    def delete_all_checkpoints(self, path):
        pass
