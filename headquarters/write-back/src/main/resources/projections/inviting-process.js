fromStreams(['$ce-Calendar', '$ce-OrganizerReservation']).
    when({
        'ecommerce.scheduling.calendar.CalendarCreated' : function(s,e) {
            linkTo('inviting', e);
        },
        'ecommerce.scheduling.ReservationCreated' : function(s,e) {
            linkTo('inviting', e);
        },
        'ecommerce.scheduling.AttendeeInvited' : function(s,e) {
            linkTo('inviting', e);
        }
    });