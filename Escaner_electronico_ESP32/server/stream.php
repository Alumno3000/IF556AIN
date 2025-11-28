<?php
header("Content-Type: text/event-stream");
header("Cache-Control: no-cache");

require "database.php";

$sql = "SELECT * FROM ble_data ORDER BY id DESC LIMIT 20";
$res = $conn->query($sql);

$rows = [];

while ($r = $res->fetch_assoc()) {
    $rows[] = $r;
}

echo "data: " . json_encode($rows) . "\n\n";
flush();
?>

