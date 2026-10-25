; BlockCraft Windows installer — Inno Setup 6
#define MyAppName "BlockCraft"
#define MyAppVersion "0.2.0.0"
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
ShowLanguageDialog=auto
UsePreviousAppDir=yes
UsePreviousGroup=yes
Uninstallable=yes
UninstallDisplayName=BlockCraft
SetupLogging=yes
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

[Types]
Name: "full"; Description: "Full installation"
Name: "compact"; Description: "Compact installation"
Name: "custom"; Description: "Custom installation"; Flags: iscustom

[Components]
Name: "game"; Description: "BlockCraft game and runtime"; Types: full compact custom
Name: "resourcepacks"; Description: "Original textures, models and sound packs"; Types: full
Name: "documentation"; Description: "Documentation and troubleshooting"; Types: full

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: unchecked
Name: "startmenuicon"; Description: "Create a Start Menu shortcut"; GroupDescription: "Additional shortcuts:"; Flags: checkedonce

[Files]
Source: "..\build\package\BlockCraft\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: game
Source: "..\src\main\resources\*"; DestDir: "{app}\resources"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist; Components: resourcepacks
Source: "..\README.md"; DestDir: "{app}\documentation"; Flags: ignoreversion; Components: documentation
Source: "..\LICENSE"; DestDir: "{app}\documentation"; Flags: ignoreversion; Components: documentation

[Icons]
Name: "{group}\BlockCraft"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{autodesktop}\BlockCraft"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon
Name: "{group}\BlockCraft Uninstall"; Filename: "{uninstallexe}"
Name: "{autoprograms}\BlockCraft\BlockCraft"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: startmenuicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch BlockCraft"; Flags: postinstall nowait skipifsilent
