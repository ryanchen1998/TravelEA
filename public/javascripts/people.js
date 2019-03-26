
$(document).ready(function () {
    //Initialises the data table and adds the filter button to the right of the search field
    $('#dtPeople').DataTable( {
        dom: "<'row'<'col-sm-12 col-md-2'l><'col-sm-12 col-md-9'bf><'col-sm-12 col-md-1'B>>" +
            "<'row'<'col-sm-12'tr>>" +
            "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
        buttons: [
            {
                text: 'Filter',
                action: function ( e, dt, node, config ) {
                    $('#modalContactForm').modal('toggle');
                }
            }
        ]
    } );
});

function searchParams(){
    var nationality = document.getElementById('nationality').value;
    var gender = document.getElementById('gender').value;
    var minAge = document.getElementById('minAge').value;
    var maxAge = document.getElementById('maxAge').value;
    var travellerType = document.getElementById('travellerType').value;
    var url = '/people/search?';
    if (nationality) {
        url += "nationalityId=" + nationality + "&";
    }
    if (gender) {
        url += "gender=" + gender + "&";
    }
    if (minAge) {
        url += "minAge=" + minAge + "&";
    }
    if (maxAge) {
        url += "maxAge=" + maxAge + "&";
    }

    if (travellerType) {
        url += "travellerTypeId=" + travellerType + "&";
    }
    url = url.slice(0, -1);
    return url;
}

function apply(){
    var url;
    url = searchParams();
    window.location = url;

}