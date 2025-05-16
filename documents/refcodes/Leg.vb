
<Serializable()>
Public Class Leg
    Private _Unit As String
    Private _Depth As Double
    Private _Time As TimeSpan
    Private _AscentRate As Double
    Private _DescentRate As Double
    Private _Gas As Gas
    Private _SetPoint As Double

    Public Property Depth() As Double
        Get
            Return _Depth
        End Get
        Set(value As Double)
            _Depth = value
        End Set
    End Property
    Public Property Time() As TimeSpan
        Get
            Return _Time
        End Get
        Set(value As TimeSpan)
            _Time = value
        End Set
    End Property
    Public Property AscentRate() As Double
        Get
            Return _AscentRate
        End Get
        Set(value As Double)
            _AscentRate = value
        End Set
    End Property
    Public Property DescentRate() As Double
        Get
            Return _DescentRate
        End Get
        Set(value As Double)
            _DescentRate = value
        End Set
    End Property
    Public Property Gas() As Gas
        Get
            Return _Gas
        End Get
        Set(value As Gas)
            _Gas = value
        End Set
    End Property
    Public Property SetPoint As Double
        Get
            Return _SetPoint
        End Get
        Set(value As Double)
            _SetPoint = value
        End Set
    End Property
    Public ReadOnly Property IsCC() As Boolean
        Get
            Return _Gas.IsCC()
        End Get
    End Property
    Sub New(ByVal unit As String, ByVal depth As Double, ByVal time As TimeSpan, ByVal ascentRate As Double,
            ByVal descentRate As Double, ByVal gas As Gas)
        _Depth = depth
        _Time = time
        _AscentRate = ascentRate
        _DescentRate = descentRate
        _Gas = gas
    End Sub
End Class
