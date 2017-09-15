'use strict';


export default function () {
  return function reverseFilter(collection) {
    return collection.slice().reverse();
  };
}
