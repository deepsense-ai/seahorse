'use strict';


export default function EventEmitter(payload) {
  return {
    $event: payload
  };
}
