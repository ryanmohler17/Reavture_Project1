package com.ryan.service;

import com.ryan.models.Request;
import com.ryan.models.RequestPart;
import com.ryan.models.RequestStatus;
import com.ryan.models.RequestType;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Date;

public class RequestServiceTest {

    @Test
    public void testTotalAmount() {
        double expected = 300.0;

        RequestPart requestPart1 = new RequestPart(100, "Test 1", RequestType.OTHER);
        RequestPart requestPart2 = new RequestPart(10, "Test 2", RequestType.TRAVEL_MILES);
        requestPart2.setRate(10);
        RequestPart requestPart3 = new RequestPart(100, "Test 3", RequestType.EQUIPMENT);
        Request request = new Request(1, new Date(), RequestStatus.DENIED, new Date());
        request.getParts().add(requestPart1);
        request.getParts().add(requestPart2);
        request.getParts().add(requestPart3);

        double calc = new RequestService().getTotalAmount(request);
        assertEquals(expected, calc, .2);
    }

}
