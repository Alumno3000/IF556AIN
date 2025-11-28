<?php
header("Content-Type: application/json");
require "database.php";

$data = json_decode(file_get_contents("php://input"), true);

if (!$data) {
    echo json_encode(["error" => "JSON inválido"]);
    exit;
}

$device_id = $data["device_id"];
$ble_list = $data["ble"];

foreach ($ble_list as $b) {
    $name = $b["name"];
    $mac = $b["mac"];
    $rssi = $b["rssi"];
    $dist = $b["dist"];

    $sql = "INSERT INTO ble_data(device_id, name, mac, rssi, distance, ts)
            VALUES('$device_id', '$name', '$mac', $rssi, $dist, NOW())";

    $conn->query($sql);
}

echo json_encode(["status" => "OK"]);
?>

