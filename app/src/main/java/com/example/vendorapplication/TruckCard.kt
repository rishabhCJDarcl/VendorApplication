//package com.example.vendorapplication
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//
//@Composable
//fun TruckCard(truck: Truck) {
//    Card(
//        modifier = Modifier.padding(10.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text("Truck No: ${truck.truckNumber}", style = MaterialTheme.typography.titleMedium)
//            Text("From: ${truck.from}")
//            Text("To: ${truck.to}")
//            Text("Type: ${truck.type}")
//            Text("Passing: ${truck.passing}")
//        }
//    }
//}