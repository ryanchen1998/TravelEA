var countryDict = {};

// Runs get countries method, then add country options to drop down
function fillCountryInfo(getCountriesUrl) {
    // Run a get request to fetch all destinations
    get(getCountriesUrl)
    // Get the response of the request
        .then(response => {
            // Convert the response to json
            response.json().then(data => {
                // Json data is an array of destinations, iterate through it
                for(let i = 0; i < data.length; i++) {
                    // Also add the item to the dictionary
                    countryDict[data[i]['id']] = data[i]['name'];
                }
                // Now fill the drop down box, and list of destinations
                fillDropDown();
                // updateDestinationsCountryField();
            });
        });
}

function updateDestinationsCountryField() {
    // Iterate through all destinations to add
    var tds = document.querySelectorAll('#destinationList td'), i;
    for(i = 0; i < tds.length; ++i) {
        if(tds[i].id === "country"){
            tds[i].innerHTML = countryDict[parseInt(tds[i].childNodes[0].data)]; // No idea why tds[i].value is not working
        }
    }
}

function fillDropDown() {
    for(let key in countryDict) {
        // For each destination, make a list element that is the json string of object
        let item = document.createElement("OPTION");
        item.innerHTML = countryDict[key];
        item.value = key;
        // Add list element to drop down list
        document.getElementById("countryDropDown").appendChild(item);
    }
}

function newDestination(url) {
    // Read data from destination form
    const formData = new FormData(document.getElementById("addDestinationForm"));
    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});
    // Post json data to given url
    post(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
            if (response.status != 200) {
                showErrors(json);
            } else {
                // Toggles aria-hidden to hide add destination form
                // TODO: Get toggle working
                document.getElementById("modalContactForm").setAttribute("aria-hidden", "true");
            }
});
});
}

let DEST_IDEN = [];
let IDENTIFIER = 0;

function addDestinationToTrip(dest) {
    DEST_IDEN.push([IDENTIFIER, dest[0]]);

    document.getElementById('list').insertAdjacentHTML('beforeend',
        '<div class="sortable-card" id=' + IDENTIFIER + '>\n' +
        '    <!-- Card -->\n' +
        '    <div class="card mb-4">\n' +
        '        <!--Card image-->\n' +
        '        <div class="view overlay">\n' +
        '            <img class="card-img-top" src="https://www.ctvnews.ca/polopoly_fs/1.1439646.1378303991!/httpImage/image.jpg_gen/derivatives/landscape_620/image.jpg" alt="Card image cap">\n' +
        '            <a href="#!">\n' +
        '                <div class="mask rgba-white-slight"></div>\n' +
        '            </a>\n' +
        '        </div>\n' +
        '        <!--Card content-->\n' +
        '        <div class="card-body">\n' +
        '            <!--Title-->\n' +
        '            <h4 class="card-title"> ' + dest[1] + '</h4>\n' +
        '            <!--Text-->\n' +
        '            <p class="card-text">' +
        '                <b>Type: </b> '+ dest[2] + '<br/>' +
        '                <b>District: </b> '+ dest[3] + '<br/>' +
        '                <b>Latitude: </b>' + dest[4] + '<br/>' +
        '                <b>Longitude: </b>' + dest[5] + '<br/>' +
        '                <b>Country: </b>' + countryDict[dest[6]] +
        '            </p>\n' +
        '            <form id="arrivalDepartureForm">\n' +
        '                <div class="modal-body mx-3">\n' +
        '                    <div>Arrival\n' +
        '                        <i class="fas prefix grey-text"></i>\n' +
        '                        <input type="date" name="arrivalDate" class="form-control validate"><input type="time" name="arrivalTime" class="form-control validate">\n' +
        '                        <label id="arrivalError"></label>\n' +
        '                    </div>\n' +
        '                    <div>Departure\n' +
        '                        <i class="fas prefix grey-text"></i>\n' +
        '                        <input type="date" name="departureDate" class="form-control validate"><input type="time" name="departureTime" class="form-control validate">\n' +
        '                        <label id="departureError"></label>\n' +
        '                    </div>\n' +
        '                </div>\n' +
        '            </form>\n' +
        '            <label id="destinationError"></label>\n' +
        '            <!-- Provides extra visual weight and identifies the primary action in a set of buttons -->\n' +
        '            <button type="button" class="btn btn-primary btn-md" onclick="removeDestinationFromTrip(IDENTIFIER)">Remove</button>\n' +
        '        </div>\n' +
        '    </div>\n' +
        '</div>'
    );

    IDENTIFIER++;
}

// TODO: Get errors displaying correctly
function removeDestinationFromTrip(cardId) {
    let destinations = Array.of(document.getElementById("sortable").children)[0];

    for (let i = 0; i < destinations.length; i++) {
        if (destinations[i].getAttribute("id") === cardId) {
            destinations[i].parentNode.removeChild(destinations[i]);
            break;
        }
    }
}

function createTrip(url, redirect) {
    let listItemArray = Array.of(document.getElementById("sortable").children);
    let tripDataList = [];

    for (let i = 0; i < listItemArray[0].length; i++) {
        tripDataList.push(listItemToTripData(listItemArray[0][i], i));
    }

    let tripData = {
        "uid": 0,    // TODO: Fix uid, arrivalTime and departureTime
        "tripDataCollection": tripDataList
    };

    post(url, tripData).then(response => {
        // Read response from server, which will be a json object
        response.json()
            .then(json => {
                if (response.status === 400) {
                    showErrors(json);    // TODO: Fix show errors
                } else if (response.status === 200) {
                    window.location.href = redirect;
                } else {
                    document.getElementById("destinationError").innerHTML = "Error(s): " + Object.values(json).join(", ");
                }
            });
        });
}

function listItemToTripData(listItem, index) {
    // Create json object to store data
    let json = {};

    // Assign position to be equal to given index
    json["position"] = index;

    // Read destination id
    let destIdentifier = listItem.getAttribute("id");

    for (let i = 0; i < DEST_IDEN.length; i++) {
        if (DEST_IDEN[i][0] === parseInt(destIdentifier)) {
            json["destinationId"] = DEST_IDEN[i][1];
            break;
        }
    }

    // Fill in arrival time (if any)
    try { json["arrivalTime"] = new Date(listItem.children.arrivalInput.value).toISOString(); }
    catch {json["arrivalTime"] = null }

    // Fill in departure time (if any)
    try { json["departureTime"] = new Date(listItem.children.departureInput.value).toISOString(); }
    catch {json["departureTime"] = null }

    // Return created json object
    return json;
}

// TODO: Get errors displaying correctly
function showErrors(json) {
    let listItemArray = Array.of(document.getElementById("sortable").children);

    for (const element in listItemArray[0]) {
        element.getElementById("destinationError").innerHTML = "123";
    }

    for (const key of Object.keys(json)) {
        let errors = json[key].substr(0, Math.max(0, json[key].length - 2)).split(', ');

        if (errors[0] != null) {
            listItemArray[0][parseInt(key)].getElementById("destinationError").innerHTML = errors[0];
        }
    }
}

function addTripToServer(url, redirect) {
    // Create array (which will be serialized)
    let tripDataCollection = [];

    // Get user id to use
    let uid = document.getElementById("userIDInput").value;

    // For each list item in list, convert it to a json object and add it to json array
    var listItemArray = Array.of(document.getElementById("tripStageList").children);
    for(let i = 0; i < listItemArray[0].length; i++) {
        tripDataCollection.push(listItemToTripData(listItemArray[0][i], i));
    }

    // Now create final json object
    let data = {};
    data["uid"] = uid;
    data["tripDataCollection"] = tripDataCollection;
    console.log(data);
    // Post json object to server at given url
    post(url,data)
        .then(response => {
            // Read response from server, which will be a json object
            response.json()
                .then(json => {
                    if(response.status == 400) {
                        showErrors(json);
                    } else if (response.status == 200) {
                        window.location.href = redirect + JSON.parse(json);
                    } else {
                        document.getElementById("errorDisplay").innerHTML = "Error(s): " + Object.values(json).join(", ");
                    }
                });
        });
}

let idCount = 0;
let tripId = -1;

function deleteListItem(itemID) {
    return () => {
        document.getElementById("tripStageList").removeChild(document.getElementById(itemID));
        console.log("Removed stage row with id " + itemID);
    }
}

function moveItemUp(itemID) {
    return () => {
        // First get index of LI in list
        let listArray = Array.from(document.getElementById("tripStageList").children);
        let index = listArray.indexOf(document.getElementById(itemID));

        // Check if highest element
        if(index == 0) {
            return;
        }

        // Swap array elements
        let b = listArray[index];
        listArray[index] = listArray[index-1];
        listArray[index-1] = b;

        // Clear the list
        document.getElementById("tripStageList").innerHTML = '';

        // Fill list from swapped array
        for (let i = 0; i < listArray.length; i++){
            document.getElementById("tripStageList").appendChild(listArray[i]);
        }
    }
}

function moveItemDown(itemID){
    return () => {
        // First get index of LI in list
        let listArray = Array.from(document.getElementById("tripStageList").children);
        let index = listArray.indexOf(document.getElementById(itemID));

        // Check if highest element
        if(index == listArray.length - 1) {
            return;
        }

        // Swap array elements
        let b = listArray[index];
        listArray[index] = listArray[index+1];
        listArray[index+1] = b;

        // Clear the list
        document.getElementById("tripStageList").innerHTML = '';

        // Fill list from swapped array
        for (let i = 0; i < listArray.length; i++){
            document.getElementById("tripStageList").appendChild(listArray[i]);
        }

    }
}



function addTripToServer(url, redirect) {
    // Create array (which will be serialized)
    let tripDataCollection = [];

    // Get user id to use
    let uid = document.getElementById("userIDInput").value;

    // For each list item in list, convert it to a json object and add it to json array
    var listItemArray = Array.of(document.getElementById("tripStageList").children);
    for(let i = 0; i < listItemArray[0].length; i++) {
        tripDataCollection.push(listItemToTripData(listItemArray[0][i], i));
    }

    // Now create final json object
    let data = {};
    data["uid"] = uid;
    data["tripDataCollection"] = tripDataCollection;
    console.log(data);
    // Post json object to server at given url
    post(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
        if(response.status == 400) {
        showErrors(json);
    } else if (response.status == 200) {
        window.location.href = redirect + JSON.parse(json);
    } else {
        document.getElementById("errorDisplay").innerHTML = "Error(s): " + Object.values(json).join(", ");
    }
});
});
}

function fillExistingTripData(url) {
    tripId = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);
    console.log(url + tripId);
    // Run a get request to fetch all destinations
    get(url + tripId)
    // Get the response of the request
        .then(response => {
        // Convert the response to json
        response.json().then(data => {
        console.log(data);
    document.getElementById("userIDInput").value = data["uid"];
    document.getElementById("userIDInput").disabled = true;
    // Json data is an array of tripData, iterate through it
    for(var i = 0; i < data["tripDataCollection"].length; i++) {
        // For each tripData, add the corresponding row to tripStageList
        let obj = data["tripDataCollection"][i];

        addNewStageRow(
            obj["destinationId"],
            (obj["arrivalTime"] == null) ? null : new Date(obj["arrivalTime"]).toISOString().slice(0, -1),
            (obj["departureTime"] == null) ? null : new Date(obj["departureTime"]).toISOString().slice(0, -1));
    }
});
});
}

function updateTripOnServer(url) {
    // Create array (which will be serialized)
    let tripDataCollection = [];

    // Get user id to use
    let uid = document.getElementById("userIDInput").value;

    // For each list item in list, convert it to a json object and add it to json array
    var listItemArray = Array.of(document.getElementById("tripStageList").children);
    for(let i = 0; i < listItemArray[0].length; i++) {
        tripDataCollection.push(listItemToTripData(listItemArray[0][i], i));
    }

    // Now create final json object
    let data = {};
    data["uid"] = uid;
    data["tripDataCollection"] = tripDataCollection;
    data["id"] = tripId;
    console.log(data);
    // Post json object to server at given url
    put(url,data)
        .then(response => {
        // Read response from server, which will be a json object
        response.json()
        .then(json => {
        if(response.status == 400) {
        showErrors(json);
    } else if (response.status == 200) {
        document.getElementById("errorDisplay").innerHTML = "Successfully updated trip";
    } else {
        document.getElementById("errorDisplay").innerHTML = "Error(s): " + Object.values(json).join(", ");
    }
});
});
}

function getAllTripsOfUser(url, viewUrl) {
    userId = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);
    // Run a get request to fetch all destinations
    get(url + userId)
    // Get the response of the request
        .then(response => {
        // Convert the response to json
        response.json().then(data => {
        // Json data is an array of tripData, iterate through it
        for(var i = 0; i < data["allTrips"].length; i++) {
        // For each tripData, add the corresponding row to tripStageList
        let obj = data["allTrips"][i];

        var a = document.createElement('a');
        var linkText = document.createTextNode("Trip ID: " + obj["id"] + ", total destinations: " + obj["tripDataCollection"].length);
        a.appendChild(linkText);
        a.title = "Trip ID: " + obj["id"] + ", total destinations: " + obj["tripDataCollection"].length;
        a.href = viewUrl + obj["id"];
        document.getElementById("tripList").appendChild(a);
        document.getElementById("tripList").appendChild(document.createElement("BR"));
    }
});
});
}

function post(url, data) {
    return fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

function put(url, data) {
    return fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
}

function get(url) {
    return fetch(url, {
        method: "GET"
    })
}

function showErrorsDefault(json) {
    const elements = document.getElementsByTagName("pre");
    console.log(json);
    for (i in elements) {
        elements[i].innerHTML = "";
    }
    for (const key of Object.keys(json)) {
        document.getElementById("errorDisplay").innerHTML += (parseInt(key) + 1) + " " + json[key];
    }
}
