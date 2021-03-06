/**
 * Types of request recognised by undo redo stack.
 */
const requestTypes = {
    "CREATE": 1,
    "UPDATE": 2,
    "TOGGLE": 3,
};

/**
 * Acts as an interface for what should be put into the UndoRedoReq.
 *
 * @param {Number} type type of request represented by requestTypes
 * @param {String} URL address that request is ment to be delivered
 * @param {Object} handler the function to pass the response through
 * @param {Object} body body of request if applicable
 */
class ReqData {
    constructor(type, URL, handler, body = {}) {
        this.type = type;
        this.URL = URL;
        this.handler = handler;
        this.body = body;
    }
}

/**
 * This acts as an interface as too what gets put on the undo redo stack.
 *
 * @param {Object} undoReqData ReqData object for redo request
 * @param {Object} redoReqData ReqData object for undo request
 */
class UndoRedoReq {
    constructor(undoReqData, redoReqData) {
        this.undoReq = undoReqData;
        this.redoReq = redoReqData;
    }
}

/**
 * Stack for request data.
 */
class ReqStack {
    constructor() {
        this.stack = [];
    }

    /**
     * pushes data onto stack.
     *
     * @param {Object} undoRedoReq UndoRedoReq object to be pushed onto stack
     */
    push(undoRedoReq) {
        this.stack.push(undoRedoReq);
    }

    /**
     * pops an object of the top of the stack.
     */
    pop() {
        // Maybe we will do stuff here
        return this.stack.pop();
    }

    /**
     * Returns true if the stack is empty, false otherwise
     *
     * @returns {boolean} true if the stack is empty, false otherwise
     */
    isEmpty() {
        return this.stack.length === 0;
    }

    /**
     * Clears the stack
     */
    clear() {
        this.stack = [];
    }

}

/**
 * This is the main structure for managing undo/redo by having two stacks.
 */
class UndoRedo {
    constructor() {
        this.undoStack = new ReqStack();
        this.redoStack = new ReqStack();
    }

    /**
     * An intermediary step for sending requests to the api.
     * Will store this request as a redoReq and create an inverse undoReq.
     *
     * @param {Object} reqData ReqData instance that contains data for request to send
     * @param {Object} inverseHandler the function to pass the response through when executing the reverse action
     */
    sendAndAppend(reqData, inverseHandler = null) {
        return this.resAndInverse(reqData, inverseHandler).then(
            ({status, json, inverseData}) => {
                reqData.handler(status, json);
                if (status !== 201 && status !== 200) {
                    // Handler should take care of this case
                    return;
                }
                const undoRedoReq = new UndoRedoReq(inverseData, reqData);
                this.undoStack.push(undoRedoReq);
                updateUndoRedoButtons();
            });
    }

    /**
     * Handles the next undo and will add it to redo stack. Throws error if no undos.
     */
    undo() {
        const undoRedoReq = this.undoStack.pop();
        if (!undoRedoReq) {
            toast('Undo', 'Nothing to undo!', 'danger');
            updateUndoRedoButtons();
            throw "No undo's";
        }

        return this.resAndInverse(undoRedoReq.undoReq).then(
            ({status, json, inverseData}) => {
                undoRedoReq.undoReq.handler(status, json);
                if (status !== 201 && status !== 200) {
                    //Handler should take care of this case
                    return;
                }
                this.redoStack.push(
                    new UndoRedoReq(undoRedoReq.undoReq, inverseData));
                toast('Undo', 'Undo successful');
                updateUndoRedoButtons();
            });

    }

    /**
     * Handles the next redo and will add it to the undo stack. Throws error if no redos.
     */
    redo() {
        const undoRedoReq = this.redoStack.pop();
        if (!undoRedoReq) {
            toast('Redo', 'Nothing to redo!', 'danger');
            updateUndoRedoButtons();
            throw "No redos";
        }

        return this.resAndInverse(undoRedoReq.redoReq).then(
            ({status, json, inverseData}) => {
                undoRedoReq.redoReq.handler(status, json);
                if (status !== 201 && status !== 200) {
                    //Handler should take care of this case
                    return;
                }
                this.undoStack.push(
                    new UndoRedoReq(inverseData, undoRedoReq.redoReq));
                toast('Redo', 'Redo successful');
                updateUndoRedoButtons();
            });
    }

    /**
     * Generates a response by calling the appropriate fetch method and creates and
     * inverse data set to be used for undo/redo.
     *
     * @param {Object} reqData ReqData instance that contains data for request to send
     * @param {Object} inverseHandler the function to pass the response through when executing the reverse action
     */
    resAndInverse(reqData, inverseHandler = null) {
        switch (reqData.type) {
            case requestTypes["TOGGLE"]:
                //Delete should toggle so its inverse is itself
                return put(reqData.URL, reqData.body).then(sponse => {
                    return sponse.json().then(json => {
                        return {
                            status: sponse.status,
                            json,
                            inverseData: reqData
                        };
                    });
                });

            case requestTypes["CREATE"]:
                //Send a post request with req data and generate a delete toggle
                inverseHandler = inverseHandler || reqData.handler;
                return post(reqData.URL, reqData.body).then(sponse => {
                    return sponse.json().then(json => {
                        const inverseData = new ReqData(requestTypes['TOGGLE'],
                            `${reqData.URL}/${json}/delete`, inverseHandler);
                        return {status: sponse.status, json, inverseData};
                    });
                });
            case requestTypes["UPDATE"]:
                return put(reqData.URL, reqData.body).then(sponse => {
                    return sponse.json().then(json => {
                        const inverseData = {
                            ...reqData,
                            body: json,
                        };
                        return {
                            status: sponse.status,
                            json,
                            inverseData,
                        };
                    });
                });
            default:
                throw "Request type not found";
        }
    }
}

//Initialise
const undoRedo = new UndoRedo();

//Add custom key shortcuts
document.onkeydown = (e) => {
    //Need to keep depreciated symbols for older browsers
    const key = e.which || e.keyCode || e.key;
    if (e.ctrlKey && key === 90) {
        //ctrl + z
        undoRedo.undo();
    } else if (e.ctrlKey && key === 89) {
        //ctrl + y
        undoRedo.redo();
    }
};

//Code for testing only
//this will only be imported if run by node
if (typeof module !== 'undefined' && module.exports) {
    try {
    var {put, post, _delete} = require('./fetch');
    //Create duud dependencies
    var toast = () => {
    };
    var updateUndoRedoButtons = () => {
    };
        module.exports = {
            ReqStack,
            UndoRedo,
            UndoRedoReq,
            ReqData,
            requestTypes
        };
    } catch (e) {
    }
}