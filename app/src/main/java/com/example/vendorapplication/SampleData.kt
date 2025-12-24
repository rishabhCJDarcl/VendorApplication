package com.example.vendorapplication



fun getSampleTrucks(): List<Truck> {
    return listOf(
        Truck("MH12AB1234", "Pune", "Mumbai", "Container", "National"),
        Truck("GJ05CD4556", "Surat", "Delhi", "Trailer", "All India"),
        Truck("RJ14EF8877", "Jaipur", "Gurgaon", "Tanker", "State"),
        Truck("MH16XY9900", "Nagpur", "Hyderabad", "Open Body", "National")
    )
}