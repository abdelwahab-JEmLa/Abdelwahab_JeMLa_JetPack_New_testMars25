package Learn

//@Composable
//fun WellnessScreen(modifier: Modifier = Modifier) {
//    WaterCounter(modifier)
//}
//
//@Composable
//fun WaterCounter(modifier: Modifier = Modifier) {
//    Column(modifier = modifier.padding(16.dp)) {
//        var count by remember { mutableStateOf(0) }
//
//        if (count > 0) {
//            // This text is present if the button has been clicked
//            // at least once; absent otherwise
//            Text("You've had $count glasses.")
//        }
//        Button(onClick = { count++ }, Modifier.padding(top = 8.dp)) {
//            Text("Add one")
//        }
//    }
//}
//@Composable
//fun ScreenContent(modifier: Modifier = Modifier) {
//

//    Text(
//            modifier = Modifier
//                .padding(16.dp)
//                .background(color = Color.Red, shape = RoundedCornerShape(15.dp))
//                .border(width = 2.dp, color = Color.Blue, shape = RoundedCornerShape(15.dp))
//                .padding(16.dp)
//                .width(300.dp)
//                .height(200.dp)
//                .background(color = Color.Red)
//                .
//                ,
//            text = "Text 1"
//        )

//    Button(
//        onClick = {
//            Log.d("MyButton", "Clicked")
//        },
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.White,
//            contentColor = Color.Red,
//            disabledContainerColor = Color.Gray,
//            disabledContentColor = Color.Black
//        ),
//        enabled = true,
//        shape = RoundedCornerShape(4.dp),
//        modifier = modifier.width(300.dp)
//    ) {
//        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
//        Text(text = "Button")
//    }
//
//

//
//    Row(
//        modifier = Modifier.fillMaxSize(),
//
//        ) {
//        Text(
//            modifier = Modifier
//                .background(color = Color.Red)
//                .weight(0.2f),
//            text = "Text 1"
//        )
//        Text(
//            modifier = Modifier
//                .background(color = Color.Blue)
//                .weight(0.5f),
//            text = "Text 2"
//        )
//        Text(
//            modifier = Modifier
//                .background(color = Color.Green)
//                .weight(0.3f),
//            text = "Text 3"
//        )

//    }


//}
//@Composable
//fun MyMultiline(myText: String, modifier: Modifier = Modifier, fontSize: Int = 30, fontWeight: FontWeight = FontWeight.Bold) {
//    Text(
//        text = myText,
//        modifier = modifier.width(300.dp),
//        fontSize = fontSize.sp,
//        fontWeight = fontWeight,
//        fontFamily = FontFamily.Cursive,
//        maxLines = 2,
//        textAlign = TextAlign.Center
//    )
//}