package com.logistics.backend.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalCustomers;
    private long totalDrivers;
    private long approvedDrivers;
    private long pendingDrivers;
    private long disabledUsers;

    private long totalLoads;
    private long openLoads;
    private long matchedLoads;
    private long deliveredLoads;
    private long cancelledLoads;

    private long totalRoutePlans;
    private long openRoutePlans;
    private long fullRoutePlans;
    private long inProgressRoutePlans;
    private long completedRoutePlans;
    private long cancelledRoutePlans;

    private long totalOffers;
    private long pendingOffers;
    private long acceptedOffers;
    private long rejectedOffers;
    private long cancelledOffers;

    private long pendingDriverDocuments;
}
