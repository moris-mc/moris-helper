$testPipeName = 'moris-presence-test-' + [guid]::NewGuid().ToString('N')
$testJob = Start-Job -ArgumentList $testPipeName -ScriptBlock {
 param($name)
 $server = [System.IO.Pipes.NamedPipeServerStream]::new($name,[System.IO.Pipes.PipeDirection]::InOut,1,[System.IO.Pipes.PipeTransmissionMode]::Byte,[System.IO.Pipes.PipeOptions]::Asynchronous)
 try {
  Write-Output 'MOCK_READY'
  if (-not $server.WaitForConnectionAsync().Wait(15000)) { throw 'Test connection timed out' }
  $reader = [System.IO.BinaryReader]::new($server,[System.Text.Encoding]::UTF8,$true)
  $writer = [System.IO.BinaryWriter]::new($server,[System.Text.Encoding]::UTF8,$true)
  function Read-Packet {
   $opcode=$reader.ReadInt32(); $length=$reader.ReadInt32()
   if ($length -lt 0 -or $length -gt 1048576) { throw 'Invalid packet length' }
   $data=$reader.ReadBytes($length)
   [pscustomobject]@{Opcode=$opcode;Data=$data;Text=[System.Text.Encoding]::UTF8.GetString($data)}
  }
  function Send-Packet([int]$opcode,[byte[]]$data) { $writer.Write($opcode);$writer.Write([int]$data.Length);$writer.Write($data);$writer.Flush() }
  $packet=Read-Packet; $handshake=$packet.Text|ConvertFrom-Json
  if ($packet.Opcode -ne 0 -or $handshake.client_id -ne '1527675374874529823') { throw 'Handshake mismatch' }
  Send-Packet 1 ([System.Text.Encoding]::UTF8.GetBytes('{"evt":"READY","data":{}}'))
  $packet=Read-Packet; $request=$packet.Text|ConvertFrom-Json
  if ($request.cmd -ne 'SET_ACTIVITY' -or $request.args.activity.details -ne '我在森友伺服器｜mcmoris.net' -or $request.args.activity.state -ne '等你一起來玩ouo!!') { throw 'Activity mismatch' }
  if ($request.args.activity.assets.large_image -ne 'https://www.mcmoris.net/media/moris-logo.webp') { throw 'Logo mismatch' }
  Send-Packet 3 ([byte[]](10,20,30))
  $packet=Read-Packet
  if ($packet.Opcode -ne 4 -or ($packet.Data -join ',') -ne '10,20,30') { throw 'PONG mismatch' }
  $packet=Read-Packet;$clear=$packet.Text|ConvertFrom-Json
  if ($null -ne $clear.args.activity) { throw 'Clear mismatch' }
  # Keep the peer open while the client starts another blocking read and closes its handle.
  Start-Sleep -Milliseconds 1000
  Write-Output 'PASS: mock Discord validated application ID, exact Chinese text, logo, PONG and clearing.'
 } finally { $server.Dispose() }
}
try {
 for ($attempt=0;$attempt -lt 100;$attempt++) {
  if ((Receive-Job $testJob -Keep) -contains 'MOCK_READY') { break }
  Start-Sleep -Milliseconds 100
 }
 & 'C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin/java.exe' -cp 'build/classes/java/main;build/classes/java/test' org.NJ.hwamaihelper.client.PresenceTransportCheck $testPipeName
 if ($LASTEXITCODE -ne 0) { throw 'Java transport check failed' }
 Receive-Job $testJob -Wait -AutoRemoveJob
} finally { $remainingJob=Get-Job -Id $testJob.Id -ErrorAction SilentlyContinue; if ($null -ne $remainingJob) { if ($remainingJob.State -eq 'Running') { Stop-Job $remainingJob }; Remove-Job $remainingJob } }
