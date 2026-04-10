Vagrant.configure("2") do |config|

  config.vm.box = "gusztavvargadr/windows-11"
  config.vm.guest = :windows
  config.vm.communicator = "winssh"

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.cpus = 2
    vb.gui = true
    vb.customize ["modifyvm", :id, "--vram", "128"]
  end

  config.vm.provision "shell", privileged: true, inline: <<-SHELL
    Write-Host "Installing Java 21..."
    winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements

    Write-Host "Installing Maven..."
    $mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
    Invoke-WebRequest -Uri $mavenUrl -OutFile "C:\\maven.zip"
    Expand-Archive -Path "C:\\maven.zip" -DestinationPath "C:\\Program Files\\Maven"

    [System.Environment]::SetEnvironmentVariable(
      "Path",
      $env:Path + ";C:\\Program Files\\Maven\\apache-maven-3.9.9\\bin",
      "Machine"
    )

    Write-Host "Setup complete."
  SHELL

  config.vm.synced_folder ".", "/vagrant", type: "virtualbox"

end