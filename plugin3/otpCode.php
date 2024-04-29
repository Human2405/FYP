<?php

    $data = json_decode(file_get_contents("php://input"));

    if ($data && isset($data->phoneNumber)) {

    $phoneNumber = $data->phoneNumber

    // Authorisation details.
	$username = "kuhen.suresh2002@gmail.com";
	$hash = "69b5ea36d33de577674b318d733c6cee6a7be8d91cd0abbb49a992194aba20e0";

	// Config variables. Consult http://api.txtlocal.com/docs for more info.
	$test = "0";

	// Data for text message. This is the text message data.
	$sender = "API Test"; // This is who the message appears to be from.
	$numbers = $phoneNumber; // A single number or a comma-seperated list of numbers
    $otp = mt_rand(100000, 999999)
	$message = "Your OTP is: " + $otp;
	// 612 chars or less
	// A single number or a comma-seperated list of numbers
	$message = urlencode($message);
	$data = "username=".$username."&hash=".$hash."&message=".$message."&sender=".$sender."&numbers=".$numbers."&test=".$test;
	$ch = curl_init('https://api.txtlocal.com/send/?');
	curl_setopt($ch, CURLOPT_POST, true);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	$result = curl_exec($ch); // This is the result from the API

    echo json_encode(['otp' => $otp]);
    
	curl_close($ch);

    }
?>