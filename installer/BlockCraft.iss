; BlockCraft Windows installer — Inno Setup 6
#define MyAppName "BlockCraft"
#define MyAppVersion "0.1.0.0"
#define MyAppPublisher "BlockCraft"
#define MyAppExeName "BlockCraft.exe"

[Setup]
AppId={{B6F99D43-5E65-4C9A-A2B2-7A8F2B7C19E1}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppComments=An independent voxel sandbox game.
DefaultDirName={autopf}\BlockCraft
DefaultGroupName=BlockCraft
DisableProgramGroupPage=yes
LicenseFile=..\LICENSE
OutputDir=..\build\installer
OutputBaseFilename=BlockCraft-Setup-{#MyAppVersion}
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog
ArchitecturesInstallIn64BitMode=x64
WizardStyle=modern
Compression=lzma2
SolidCompression=yes
UninstallDisplayIcon={app}\{#MyAppExeName}
CloseApplications=yes
RestartApplications=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Files]
Source: "..\build\package\BlockCraft\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\BlockCraft"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{autodesktop}\BlockCraft"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch BlockCraft"; Flags: postinstall nowait skipifsilent
