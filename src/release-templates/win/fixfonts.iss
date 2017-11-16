//
// makes InnoSetup use Windows system font(Tahoma on XP, Segoe UI on Vista) 
// instead of Delphi default font
// 
// usage:
// put the following lines into your iss file:
// 
// #include "fixfonts.iss"
// 
// procedure InitializeWizard();
// begin
//   SetSystemFont(WizardForm.Font);
//   FixSystemFont(WizardForm);
// end;
//
[code]
type
  TLogFont = record
    lfHeight: Longint;
    lfWidth: Longint;
    lfEscapement: Longint;
    lfOrientation: Longint;
    lfWeight: Longint;
    lfItalic: Byte;
    lfUnderline: Byte;
    lfStrikeOut: Byte;
    lfCharSet: Byte;
    lfOutPrecision: Byte;
    lfClipPrecision: Byte;
    lfQuality: Byte;
    lfPitchAndFamily: Byte;
    lfFaceName: array[0..32{LF_FACESIZE} - 1] of Char;
  end;
  
  TNonClientMetrics = record
    cbSize: LongWord;
    iBorderWidth: Integer;
    iScrollWidth: Integer;
    iScrollHeight: Integer;
    iCaptionWidth: Integer;
    iCaptionHeight: Integer;

    lfCaptionFont: TLogFont;
    iSmCaptionWidth: Integer;
    iSmCaptionHeight: Integer;
    lfSmCaptionFont: TLogFont;
    iMenuWidth: Integer;
    iMenuHeight: Integer;
    lfMenuFont: TLogFont;
    lfStatusFont: TLogFont;
    lfMessageFont: TLogFont;
    // just buffer not to override something in memory
    stuff: array[0..400] of Byte;
  end;

function SystemParametersInfo(uiAction, uiParam: LongWord;
  var NonClientMetrics: TNonClientMetrics; fWinIni: LongWord): LongBool;
  external 'SystemParametersInfoA@user32.dll stdcall';
  
function CreateFontIndirect(var p1: TLogFont): THandle;
  external 'CreateFontIndirectA@gdi32.dll stdcall';

procedure SetSystemFont(Font: TFont);
var
  NonClientMetrics: TNonClientMetrics;
  FontHandle: THandle;
begin
  if (Font.Name <> 'MS Sans Serif') and (Font.Size <> 8) then
  begin
    Exit;
  end;
  
  NonClientMetrics.cbSize := 340; // SizeOf(NonClientMetrics);
  if SystemParametersInfo(41{SPI_GETNONCLIENTMETRICS}, 0, NonClientMetrics, 0) then
  begin
    if fsBold in Font.Style then
    begin
      NonClientMetrics.lfMessageFont.lfWeight := 800;
    end;
    
    FontHandle := CreateFontIndirect(NonClientMetrics.lfMessageFont);
    Font.Handle := FontHandle;
  end else
  begin
    RaiseException('SetFormSystemFont Error!');
  end;
end;

procedure FixSystemFont(ParentComponent: TComponent);
var
  theComponent: TComponent;
  theStaticText: TNewStaticText;
  i: Integer;
begin
  for i := 0 to ParentComponent.ComponentCount - 1 do
  begin
    theComponent := ParentComponent.Components[i];
    if theComponent is TNewStaticText then
    begin
      theStaticText := TNewStaticText(theComponent);
      if not theStaticText.ParentFont then
      begin
        SetSystemFont(theStaticText.Font);
      end;
    end;
    FixSystemFont(theComponent);
  end;
end;

