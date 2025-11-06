// eventBus.js  
const EventBus = {  
    events: {},  
    
    $on(eventName, callback) {  
      if (!this.events[eventName]) {  
        this.events[eventName] = [];  
      }  
      this.events[eventName].push(callback);  
    },  
    
    $emit(eventName, ...args) {  
      if (this.events[eventName]) {  
        this.events[eventName].forEach(callback => {  
          callback(...args);  
        });  
      }  
    },  
    
    $off(eventName, callback) {  
      if (!eventName && !callback) {  
        this.events = {};  
      } else if (callback) {  
        const callbacks = this.events[eventName];  
        if (callbacks) {  
          const index = callbacks.indexOf(callback);  
          if (index !== -1) {  
            callbacks.splice(index, 1);  
          }  
        }  
      } else {  
        this.events[eventName] = [];  
      }  
    }  
  };  
    
  export default EventBus;