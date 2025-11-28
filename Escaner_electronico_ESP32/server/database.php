<?php
$host = "localhost";
$user = "root";
$pass = "12345678";
$dbname = "esp32_scanner";

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    die("Error conexión: " . $conn->connect_error);
}
?>

