Imports System.Drawing

<Serializable()>
Public Class Gas

    Private _Units As String
    Private _ID As Integer
    Private _Name As String
    Private _FO2 As Integer
    Private _FHe As Integer
    Private _PPO2Max As Double
    Private _Volume As Double
    Private _PercentReserve As Double
    Private _Available As Boolean
    Private _Color As Color
    Private _UsedGas As Boolean
    Private _Capacity As Double
    Private _IsCapacityAvailable As Boolean = False
    Private _TimeOfReserveWarning As TimeSpan = New TimeSpan(0)
    Public ReserveWarningRaised As Boolean = False
    Private _TimeOfOutOfGasWarning As TimeSpan = New TimeSpan(0)
    Public OutOfGasWarningRaised As Boolean = False
    Private _OpenCircuit As Boolean = True

    Public Property Units() As String
        Get
            Return _Units
        End Get
        Set(value As String)
            _Units = value
        End Set
    End Property
    Public Property ID() As Integer
        Get
            Return _ID
        End Get
        Set(value As Integer)
            _ID = value
        End Set
    End Property
    Public ReadOnly Property Name() As String
        Get
            If _FO2 = 21 And _FHe = 0 Then
                Return "AIR"
            ElseIf _FO2 = 100 And _FHe = 0 Then
                Return "OXYGEN"
            ElseIf _FO2 > 21 And _FO2 < 100 And _FHe = 0 Then
                Return "NX " & _FO2
            ElseIf _FO2 + FHe = 100 Then
                Return "HX " & _FO2
            Else
                Return "TX " & _FO2 & "/" & _FHe
            End If
        End Get
    End Property
    Public ReadOnly Property NameWithOCCC() As String
        Get
            Dim additionalText = " (OC)"
            If (IsCC) Then
                additionalText = " (CC)"
            End If

            Return Name() + additionalText
        End Get
    End Property
    Public Property FO2() As Integer
        Get
            Return _FO2
        End Get
        Set(value As Integer)
            _FO2 = value
        End Set
    End Property
    Public Property FHe() As Integer
        Get
            Return _FHe
        End Get
        Set(value As Integer)
            _FHe = value
        End Set
    End Property
    Public Property UsedGas() As Boolean
        Get
            Return _UsedGas
        End Get
        Set(value As Boolean)
            _UsedGas = value
        End Set
    End Property
    Public ReadOnly Property FN2() As Integer
        Get
            Return 100 - _FHe - _FO2
        End Get
    End Property
    Public Property Capacity As Double
        Get
            Return _Capacity
        End Get
        Set(value As Double)
            _Capacity = value
        End Set
    End Property
    Public Property IsCapacityAvailable() As Boolean
        Get
            Return _IsCapacityAvailable
        End Get
        Set(value As Boolean)
            _IsCapacityAvailable = value
        End Set
    End Property
    Public ReadOnly Property IsOnReserve() As Boolean
        Get
            If (IsCapacityAvailable) Then
                Dim resCapa = _Volume * PercentReserve
                If (_Capacity <= resCapa) Then
                    Return True
                End If
            End If
            Return False
        End Get
    End Property
    Private Function Pamb(ByVal depth As Double) As Double
        Return depth / 33 + 1
    End Function

    Public ReadOnly Property PO2(ByVal Depth As Double)
        Get
            Return ((Depth / 33) + 1) * FO2 / 100
        End Get
    End Property
    Public ReadOnly Property PO2(ByVal Depth As Double, ByVal setpoint As Double)
        Get
            If (IsCC()) Then
                Dim pamb = Me.Pamb(Depth)
                If (pamb < setpoint) Then Return pamb
                Return setpoint
            Else
                Return PO2(Depth)
            End If
        End Get
    End Property
    Public ReadOnly Property DepthOfPO2(ByVal po2 As Double, ByVal setpoint As Double)
        Get
            If (IsCC()) Then
                If (setpoint <= 1) Then Return 0
                If (po2 <= setpoint) Then
                    Return 33 * (po2 - 1)
                End If
                Return -1   ' Impossible
            Else
                Return 33 * ((po2 * 100 / FO2) - 1)
            End If
        End Get
    End Property
    Public ReadOnly Property ENDa(ByVal Depth As Double, ByVal Settings As Settings) As Double
        Get
            Dim res As Double
            If (Settings.AlarmO2Nenabled) Then
                res = 33 * ((((Depth / 33) + 1) * (FN2 + FO2) / 100) - 1)
            Else
                res = 33 * ((((Depth / 33) + 1) * FN2 / 79) - 1)
            End If

            If (res < 0) Then
                Return 0
            End If

            Return res
        End Get
    End Property
    Public ReadOnly Property EAD(ByVal Depth)
        Get
            Dim sett As Settings = New Settings()
            sett.AlarmO2Nenabled = False
            Return ENDa(Depth, sett)
        End Get
    End Property
    Public Function WOB(ByVal Depth As Double) As Double
        Dim res = 33 * ((((((FN2 / 100) + 0.167) * (1 + (Depth / 33))) + (1.167 * PO2(Depth))) / 1.202) - 1)
        If (res < 0) Then
            Return 0
        End If
        Return res
    End Function


    Public ReadOnly Property TotalUsedGas()
        Get
            Return _Volume - Capacity
        End Get
    End Property
    Public ReadOnly Property GTR(ByVal depth As Double, ByVal rmv As Double) As TimeSpan
        Get
            Dim mins As Double = _Capacity / ((depth / 33) + 1) / rmv
            If (Double.IsNaN(mins)) Then Return New TimeSpan(0)
            Return New TimeSpan(0, 0, mins * 60)
        End Get
    End Property
    Public ReadOnly Property IsOC() As Boolean
        Get
            Return _OpenCircuit
        End Get
    End Property
    Public ReadOnly Property IsCC() As Boolean
        Get
            Return Not _OpenCircuit
        End Get
    End Property
    Public WriteOnly Property setOC As Boolean
        Set(value As Boolean)
            _OpenCircuit = value
        End Set
    End Property
    Public ReadOnly Property Volume As Double
        Get
            Return _Volume
        End Get
    End Property

    Public Property PPO2Max() As Double
        Get
            Return _PPO2Max
        End Get
        Set(value As Double)
            _PPO2Max = value
        End Set
    End Property
    Public Property PercentReserve() As Double
        Get
            Return _PercentReserve
        End Get
        Set(value As Double)
            _PercentReserve = value
        End Set
    End Property
    Public Property Available() As Boolean
        Get
            Return _Available
        End Get
        Set(value As Boolean)
            _Available = value
        End Set
    End Property
    Public Property Color() As Color
        Get
            Return _Color
        End Get
        Set(value As Color)
            _Color = value
        End Set
    End Property
    Public Property TimeOfReserveWarning As TimeSpan
        Get
            Return _TimeOfReserveWarning
        End Get
        Set(value As TimeSpan)
            _TimeOfReserveWarning = value
        End Set
    End Property
    Public Property TimeOfOutOfGasWarning As TimeSpan
        Get
            Return _TimeOfOutOfGasWarning
        End Get
        Set(value As TimeSpan)
            _TimeOfOutOfGasWarning = value
        End Set
    End Property
    Public ReadOnly Property SetPointMinDepth(ByVal setpoint As Double) As Double ' Min Depth at which the Set Point is reached
        Get
            Return (setpoint - 1) * 33
        End Get
    End Property
    Public ReadOnly Property PN2(ByVal depth As Double, ByVal setpoint As Double, ByVal pamb As Double) As Double
        Get
            ' Depth where setpoint equals the diluent's PO2
            Dim maxDepth = 33 * ((setpoint / Me.FO2 * 100) - 1)

            If (IsOC) Then
                ' Value for open circuit
                Return ((depth + pamb) / 33) * FN2 / 100
            Else
                If (depth <= SetPointMinDepth(setpoint)) Then
                    ' Breathing pure oxygen
                    Return 0
                ElseIf (depth <= maxDepth) Then
                    ' Breathing a composition of O2 and diluent (Me)
                    Return CCDiluentProportion(depth, setpoint) * Me.FN2 / 100 * ((depth + pamb) / 33)
                Else
                    ' Breathing a mix of O2 + diluent where O2 is lower because not renewed
                    Dim pamb2 = (depth + pamb) / 33
                    Dim diluentPressure = pamb2 - setpoint
                    Return diluentPressure * (FN2 / (FN2 + FHe))
                End If
            End If
        End Get
    End Property
    Public ReadOnly Property PHe(ByVal depth As Double, ByVal setpoint As Double, ByVal pamb As Double) As Double
        Get
            ' Depth where setpoint equals the diluent's PO2
            Dim maxDepth = 33 * ((setpoint / Me.FO2 * 100) - 1)

            If (IsOC) Then
                ' Value for open circuit
                Return ((depth / 33) + 1) * FHe / 100
            Else
                If (depth <= SetPointMinDepth(setpoint)) Then
                    ' Breathing pure oxygen
                    Return 0
                ElseIf (depth <= maxDepth) Then
                    ' Breathing a composition of O2 and diluent (Me)
                    Return CCDiluentProportion(depth, setpoint) * Me.FHe / 100 * ((depth + pamb) / 33)
                Else
                    ' Breathing a mix of O2 + diluent where O2 is lower because not renewed
                    Dim pamb2 = (depth + pamb) / 33
                    Dim diluentPressure = pamb2 - setpoint
                    Return diluentPressure * (FHe / (FN2 + FHe))
                End If
            End If
        End Get
    End Property
    Private ReadOnly Property CCDiluentProportion(ByVal depth As Double, ByVal setpoint As Double) As Double
        Get
            Dim pressure = (depth / 33) + 1
            Return ((setpoint / pressure) - 1) / ((FO2 / 100) - 1)
        End Get
    End Property
    Private ReadOnly Property CCOxygenProportion(ByVal depth As Double, ByVal setpoint As Double) As Double
        Get
            Return 1 - CCDiluentProportion(depth, setpoint)
        End Get
    End Property

    Sub New()
        _Units = "0"
        _ID = 0
        _FO2 = 21
        _FHe = 0
        _PPO2Max = 1.4
        _PercentReserve = 10
        _Available = True
        _Color = Drawing.Color.Blue
        _UsedGas = False
        _IsCapacityAvailable = False
        _Capacity = 0
        _Volume = _Capacity
    End Sub
    Sub New(ByVal units As String, _
            ByVal id As Integer, ByVal fo2 As Integer, _
            ByVal fhe As Integer, ByVal ppo2max As Double, _
            ByVal available As Boolean, _
            ByVal color As Color, ByVal capacity As Double, ByVal isCapaAvailable As Boolean, _
            ByVal reserve As Double)
        _Units = units
        _ID = id
        _FO2 = fo2
        _FHe = fhe
        _PPO2Max = ppo2max
        _PercentReserve = reserve
        _Available = available
        _Color = color
        _UsedGas = False
        _IsCapacityAvailable = isCapaAvailable
        _Capacity = capacity
        _Volume = _Capacity
    End Sub
    Sub New(ByVal units As String,
        ByVal id As Integer, ByVal fo2 As Integer,
        ByVal fhe As Integer, ByVal ppo2max As Double,
        ByVal available As Boolean,
        ByVal color As Color)
        _Units = units
        _ID = id
        _FO2 = fo2
        _FHe = fhe
        _PPO2Max = ppo2max
        _PercentReserve = PercentReserve
        _Available = available
        _Color = color
        _UsedGas = False
        _IsCapacityAvailable = False
        _Capacity = 0
        _Volume = 0
    End Sub

    Sub New(ByVal units As String,
        ByVal id As Integer, ByVal fo2 As Integer,
        ByVal fhe As Integer, ByVal ppo2max As Double,
        ByVal available As Boolean,
        ByVal color As Color, ByVal isOC As Boolean)
        _Units = units
        _ID = id
        _FO2 = fo2
        _FHe = fhe
        _PPO2Max = ppo2max
        _PercentReserve = PercentReserve
        _Available = available
        _Color = color
        _UsedGas = False
        _IsCapacityAvailable = False
        _Capacity = 0
        _Volume = 0
        Me.setOC = isOC
    End Sub

    Sub New(ByVal id As Integer, ByVal fo2 As Integer,
        ByVal fhe As Integer, ByVal ppo2max As Double,
        ByVal available As Boolean,
        ByVal isOC As Boolean)
        _Units = Units
        _ID = id
        _FO2 = fo2
        _FHe = fhe
        _PPO2Max = ppo2max
        _PercentReserve = PercentReserve
        _Available = available
        _Color = Drawing.Color.Blue     ' Default value
        _UsedGas = False
        _IsCapacityAvailable = False
        _Capacity = 0
        _Volume = 0
        Me.setOC = isOC
    End Sub

    Public Function getMOD() As Integer
        Dim res As Double = _FO2 / 100.0

        If res = 0 Then
            res = 0.0001
        End If

        res = (_PPO2Max / res) - 1.0

        If _Units = "Metric" Then
            Return Int(res * 10.0)
        Else
            Return Int(res * 33.0)
        End If

    End Function

    Public Function getENDalarmDepth(ByVal Narcotic As Boolean, ByVal ENDalarm As Double) As Integer
        Dim ENDX As Double
        Dim __FN2 As Double = (100 - _FO2 - _FHe) / 100.0
        Dim __FO2 As Double = _FO2 / 100.0

        If Narcotic Then
            ' ENDX = 33 * ((((ENDalarm / 10) + 1) * (100.0 / (_FO2 + _FN2))) - 1)
            ENDX = 33 * ((((ENDalarm / 33) + 1) * (1 / (__FO2 + __FN2))) - 1)
        Else
            ENDX = 33 * ((((ENDalarm / 33) + 1) * 0.79 / __FN2) - 1)
        End If

        If ENDX < 0 Then
            Return 0
        End If

        If ENDX >= 999 Then
            Return 999
        End If

        Return Int(ENDX)
    End Function

    Public Function getWOBalarmDepth(ByVal WOBalarm As Double) As Integer
        Dim WOBX As Double
        Dim __FN2 As Double = (100 - _FO2 - _FHe) / 100.0
        Dim __FO2 As Double = _FO2 / 100.0


        'WOBX = 33 * (((1.202 * ((WOBalarm / 33) + 1)) / (((_FN2 / 100.0) + 0.167) + (0.01167 * _FO2))) - 1)

        ' WOB alarm was recalculated as the initial formula seemed to be wrong
        WOBX = 33 * ((1.202 * ((WOBalarm / 33) + 1) / ((0.167 + __FN2) + (1.167 * __FO2))) - 1)

        If WOBX < 0 Then
            Return 0
        End If

        If WOBX >= 999 Then
            Return 999
        End If

        Return Int(WOBX)
    End Function

    Public Function getHT() As Integer
        Dim HTX As Double

        If _FO2 = 0 Then
            Return 0
        End If

        If _Units = "Metric" Then
            HTX = ((21 - _FO2) / _FO2) * 10
        Else
            HTX = ((21 - _FO2) / _FO2) * 33
        End If

        If HTX < 0 Then
            Return 0
        End If

        Return Int(HTX)
    End Function

    Public Function PPO2(ByVal Depth As Double) As Double
        Return (_FO2 / 100) * ((Depth / 33) + 1)
    End Function

    ' Called to update the gas consumption
    Public Sub consumeGas(ByVal p1 As DivePoint, ByVal p2 As DivePoint, ByVal rmv As Double)
        Dim deltaT = System.Math.Round(p2.Time.TotalSeconds - p1.Time.TotalSeconds)
        If (deltaT <= 0) Then Return
        Dim ascDesc = 60 * (p2.Depth - p1.Depth) / deltaT
        Dim consumption = (rmv / 60 * deltaT) + (rmv / 60 * p1.Depth * deltaT / 33) + (rmv / 60 * ascDesc / 60 * deltaT * deltaT / 66)
        _Capacity = _Capacity - consumption
    End Sub

    Public Sub ResetCapacity()
        _Capacity = _Volume

        ' Reset the warnings
        ReserveWarningRaised = False
        _TimeOfReserveWarning = New TimeSpan(0)
        OutOfGasWarningRaised = False
        _TimeOfOutOfGasWarning = New TimeSpan(0)
    End Sub

End Class
