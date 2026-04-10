Vagrant.configure("2") do |config|

  config.vm.box = "stromweld/windows-11"
  config.vm.guest = :windows
  config.vm.communicator = "winrm"

  config.winrm.username = "vagrant"
  config.winrm.password = "vagrant"
  config.winrm.timeout = 300

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.cpus = 2
    vb.gui = false  # headless no CI — WinRM não precisa de display
    vb.customize ["modifyvm", :id, "--vram", "128"]
    vb.customize ["modifyvm", :id, "--graphicscontroller", "vboxsvga"]
  end

  config.vm.boot_timeout = 600

  config.vm.provision "shell", privileged: true, inline: <<-SHELL
    Write-Host "Installing Java 21..."
    winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements --accept-source-agreements

    Write-Host "Installing Maven..."
    $mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
    Invoke-WebRequest -Uri $mavenUrl -OutFile "C:\\maven.zip"
    Expand-Archive -Path "C:\\maven.zip" -DestinationPath "C:\\tools\\Maven" -Force

    [System.Environment]::SetEnvironmentVariable(
      "Path",
      $env:Path + ";C:\\tools\\Maven\\apache-maven-3.9.9\\bin",
      "Machine"
    )

    Write-Host "Provisioning complete."
  SHELL

  config.vm.synced_folder ".", "/vagrant", type: "virtualbox"

end