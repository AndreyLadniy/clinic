fromStreams(['$ce-OrganizerReservation']).
    when({
        'ecommerce.scheduling.ReservationCreated' : function(s,e) {
            linkTo('inviting', e);
        },
        'ecommerce.scheduling.AttendeeInvited' : function(s,e) {
            linkTo('inviting', e);
        }
    });