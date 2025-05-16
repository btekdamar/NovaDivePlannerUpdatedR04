
<Serializable()>
Public Class Settings
    ' Contains Dive Settings only (no settings related to any app execution)
    ' All units used in the Settings and the rest of the library are IMPERIAL
    ' They are to be converted if the user sets Metric units

    Protected Const MtoFT As Double = 3.2808399
    Protected Const LtoCUFT As Double = 0.03531467

    ' Constants
    Public Const IMPERIAL As Integer = 0
    Public Const METRIC As Integer = 1
    Public Const ALT_LEVEL_SEA = 0
    'Public Const ALT_LEVEL_1 = 1
    Public Const ALT_LEVEL_2 = 2
    Public Const ALT_LEVEL_3 = 3
    Public Const ALT_LEVEL_4 = 4
    Public Const ALT_LEVEL_5 = 5
    Public Const ALT_LEVEL_6 = 6
    Public Const ALT_LEVEL_7 = 7

    Public Units As Integer
    Public AltitudeRangeLevel As Integer
    Public RMVDive As Double
    Public RMVDeco As Double
    Public LastStop As Double
    Public GFhigh As Double
    Public GFlow As Double
    Public AlarmENDenabled As Boolean
    Public AlarmENDdepth As Double
    Public AlarmWOBenabled As Boolean
    Public AlarmWOBdepth As Double
    Public AlarmO2Nenabled As Boolean
    Public ExtendedMixSwitchStop As TimeSpan

    ' Specific to Closed Circuit
    Public HighSetPoint As Double
    Public LowSetPoint As Double
    Public AutoSwitchToHighEnabled As Boolean
    Public AutoSwitchToHigh As Double
    Public AutoSwitchToLowEnabled As Boolean
    Public AutoSwitchToLow As Double


    Public ReadOnly Property Pamb
        Get
            Select Case AltitudeRangeLevel
                Case ALT_LEVEL_SEA
                    Return 33
                Case ALT_LEVEL_2
                    Return 28
                Case ALT_LEVEL_3
                    Return 26
                Case ALT_LEVEL_4
                    Return 24
                Case ALT_LEVEL_5
                    Return 22
                Case ALT_LEVEL_6
                    Return 21
                Case ALT_LEVEL_7
                    Return 18
            End Select

            Return 33
        End Get
    End Property
    Public ReadOnly Property AltLevelString As String
        Get
            Select Case AltitudeRangeLevel
                Case ALT_LEVEL_SEA
                    Return "SEA"
                Case Else
                    Return "LEVEL " + AltitudeRangeLevel.ToString()
            End Select
        End Get
    End Property
    ' Default values
    Public Sub New()
        Me.New(0, ALT_LEVEL_SEA, 0.9, 0.7, 20, 0.85, 0.3, False, 100, False, 100, False, New TimeSpan(0), _
               1.3, 0.7, False, 100, False, 50)
    End Sub

    Public Sub New(ByVal Units, ByVal AltitudeLevel, ByVal RMVDive, ByVal RMVDeco, ByVal LastStop, _
                   ByVal GFhigh, ByVal GFlow, ByVal AlarmENDenabled, ByVal AlarmENDdepth, ByVal AlarmWOBenabled, _
                   ByVal AlarmWOBdepth, ByVal AlarmO2Nenabled, ByVal extMixSwitchStop, _
                   ByVal highSetPoint, ByVal lowSetPoint, ByVal autoSwitchToHighEnabled, ByVal autoSwitchToHigh, _
                   ByVal autoSwitchToLowEnabled, ByVal autoSwitchToLow)
        Me.Units = Units
        Me.AltitudeRangeLevel = AltitudeLevel
        Me.RMVDive = RMVDive
        Me.RMVDeco = RMVDeco
        Me.LastStop = LastStop
        Me.GFhigh = GFhigh
        Me.GFlow = GFlow
        Me.AlarmENDenabled = AlarmENDenabled
        Me.AlarmENDdepth = AlarmENDdepth
        Me.AlarmWOBenabled = AlarmWOBenabled
        Me.AlarmWOBdepth = AlarmWOBdepth
        Me.AlarmO2Nenabled = AlarmO2Nenabled
        Me.ExtendedMixSwitchStop = extMixSwitchStop
        Me.HighSetPoint = highSetPoint
        Me.LowSetPoint = lowSetPoint
        Me.AutoSwitchToHighEnabled = autoSwitchToHighEnabled
        Me.AutoSwitchToHigh = autoSwitchToHigh
        Me.AutoSwitchToLowEnabled = autoSwitchToLowEnabled
        Me.AutoSwitchToLow = autoSwitchToLow
    End Sub

    Public Shared Function DefaultSettings() As Settings
        Dim settings As Settings = New Settings()
        Return settings
    End Function

    ' Return the admissible GF value for a given depth
    ' Can be called ONLY after the DeepestStop has been set (calculated with function getCeiling() )
    Public Function getGFmaxAtDepth(ByVal depth As Double, ByVal FirstDecoStopDepth As Double) As Double
        If (FirstDecoStopDepth = -1) Then
            Return GFhigh
        End If

        If (depth > FirstDecoStopDepth Or FirstDecoStopDepth = 0) Then
            Return GFlow
        End If

        Return GFlow + ((GFhigh - GFlow) * ((FirstDecoStopDepth - depth) / FirstDecoStopDepth))
    End Function

    Public Function decoStep() As Double
        If (Units = METRIC) Then Return 3 * MtoFt
        Return 10
    End Function



End Class
