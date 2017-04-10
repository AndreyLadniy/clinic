fromStreams(['$ce-Calendar', '$ce-TimeAllocationManager', '$ce-CalendarTimeManager']).
when({
    'ecommerce.scheduling.CalendarCreated' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.AttendeeTimeAllocationRequested' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.AttendeeTimeDeallocationRequested' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.AttendeeTimeReallocationRequested' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeAllocated' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeAllocatedFromQueue' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeReallocated' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeReallocatedFromQueue' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeDeallocated' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    },
    'ecommerce.scheduling.CalendarTimeDeallocatedFromQueue' : function(s,e) {
        linkTo('CalendarTimeEvents', e);
    }

});
