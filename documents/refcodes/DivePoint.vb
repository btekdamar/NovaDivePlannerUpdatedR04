
<Serializable()>
Public Class DivePoint
    ' A DivePoint is a part of the calculated DivePlan (output)

    Public Time As TimeSpan
    Public Depth As Double
    Public Settings As Settings
    Public Gas As Gas           ' Gas to be used from this point up to the next one

    'Public NDL As TimeSpan      ' Actual No Deco Limit at given dive point
    Public GF As Double         ' Actual Gradient Factor at given dive point
    Public MaxDepth As Double   ' Max Depth up to this point
    Public Ceiling As Double
    Public ENDa As Double
    Public EAD As Double
    Public OTUD As Double
    Public OTUS As Double
    Public GasCapacity As Double
    Public GasConsummed As Double
    Public GTR As TimeSpan
    Public TAT As TimeSpan
    Public SetPoint As Double = 0
    Public AutoswitchToHigh As Boolean = False
    Public AutoswitchToLow As Boolean = False
    Public NoDecoTime As TimeSpan = New TimeSpan(0)

    Public ReadOnly Property O2sat
        Get
            ' Return the max of {OTUS, OTUD}
            If (OTUS > OTUD) Then
                Return OTUS
            Else
                Return OTUD
            End If
        End Get
    End Property

    Public Sub New(ByVal settings As Settings, ByVal time As TimeSpan, ByVal depth As Double, ByVal gas As Gas)
        Me.Time = time
        Me.Depth = depth
        Me.Settings = settings
        Me.Gas = gas
        Me.GasConsummed = 0
    End Sub

    Public Function Clone() As DivePoint
        Return DirectCast(Me.MemberwiseClone(), DivePoint)
    End Function

End Class
