<?php
// Schnorr protocol parameters
$p = gmp_init('7919'); // Prime number p
$g = gmp_init('2');    // Generator g (primitive root modulo p)

$data = json_decode(file_get_contents("php://input"));

if ($data && isset($data->username)) {
    $username = $data->username;

    // Generate evidence using Schnorr protocol
    $evidence = generateEvidence($username);

    // Verify evidence using Schnorr protocol
    $isValid = verifyEvidence($username, $evidence);

    if ($isValid) {
        http_response_code(200); // Send success status code if evidence is valid
    } else {
        http_response_code(403); // Send forbidden status code if evidence is not valid
    }
} else {
    http_response_code(400); // Bad request status code if JSON data is invalid or missing fields
}

// Schnorr protocol evidence generation
function generateEvidence($username) {
    global $p, $g;
    // Generate a random private key (x)
    $x = random_bytes(16);
    // Compute public key (y = g^x mod p)
    $y = gmp_strval(gmp_powm($g, gmp_init('0x' . bin2hex($x)), $p));
    // Compute commitment (c = g^r mod p)
    $r = random_bytes(16);
    $c = gmp_strval(gmp_powm($g, gmp_init('0x' . bin2hex($r)), $p));
    // Compute challenge (e = H(y, c, m))
    $e = hash('sha256', $y . $c . $username, true);
    // Compute response (s = r - e * x mod (p-1))
    $s = gmp_strval(gmp_mod(gmp_sub(gmp_init('0x' . bin2hex($r)), gmp_mul(gmp_init('0x' . bin2hex($e)), gmp_init('0x' . bin2hex($x))), gmp_sub($p, 1)), $p));
    return json_encode(['y' => $y, 'c' => $c, 'e' => $e, 's' => $s]);
}

// Schnorr protocol verification
function verifyEvidence($username, $evidence) {
    global $p, $g;
    // Decode evidence JSON string
    $proof = json_decode($evidence);
    if (!$proof || !isset($proof->y) || !isset($proof->c) || !isset($proof->e) || !isset($proof->s)) {
        return false; // Evidence format is invalid
    }
    $y = gmp_init($proof->y);
    $c = gmp_init($proof->c);
    $e = gmp_init($proof->e);
    $s = gmp_init($proof->s);
    // Recompute commitment (c' = g^s * y^e mod p)
    $cPrime = gmp_mod(gmp_mul(gmp_powm($g, $s, $p), gmp_powm($y, $e, $p)), $p);
    // Recompute challenge (e' = H(y, c', m))
    $ePrime = hash('sha256', gmp_strval($y) . gmp_strval($cPrime) . $username, true);
    // Verify if e' matches e
    return gmp_cmp($ePrime, $e) === 0;
}

// SHA-256 hash function
function hash($data) {
    return hash('sha256', $data, true);
}
?>
