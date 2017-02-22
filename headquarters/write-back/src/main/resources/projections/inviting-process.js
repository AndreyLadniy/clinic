fromStreams(['$ce-Calendar', '$ce-TimeAllocationManager', '$ce-CalendarTimeManager']).
when({
    'ecommerce.scheduling.calendar.CalendarCreated' : function(s,e) {
        linkTo('calendarTimeEvents', e);
    },
    'ecommerce.scheduling.AttendeeTimeAllocationRequested' : function(s,e) {
        linkTo('calendarTimeEvents', e);
    },
    'ecommerce.scheduling.AttendeeTimeDeallocationRequested' : function(s,e) {
        linkTo('calendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeAllocated' : function(s,e) {
        linkTo('calendarTimeEvents', e);
    },

});