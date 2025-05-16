<Serializable()>
Public Class BuhlmannSingleTissue
    ' Fraction of N2 in Air
    Private Const FN2inAIR = 0.79
    Public Const NITROGEN = 0
    Public Const HELIUM = 1

    Private _Type As Integer = NITROGEN
    Private _Tension As Double      ' Tension given in fsw (feet)
    Private _Tau As Double          ' Time constant in min 
    Private _SurfaceTau As Double   ' Time constant used when computing surface tension (model on surface is different)
    Private _A As Double            ' Used to calculate the GF
    Private _B As Double            ' Used to calculate the GF

    Private _Pamb As Double         ' Pressure at the surface in fsw

    ' PROPERTIES
    Public Property Tension As Double
        Set(value As Double)
            _Tension = value
        End Set
        Get
            Return _Tension
        End Get
    End Property
    Public ReadOnly Property A As Double
        Get
            Return _A
        End Get
    End Property
    Public ReadOnly Property B As Double
        Get
            Return _B
        End Get
    End Property

    ' CONSTRUCTORS
    Public Sub New(ByVal Type As Integer, ByVal initialTension As Double, ByVal timeCste As Double,
                    ByVal surfaceTimeCste As Double,
                    ByVal Ai As Double, ByVal Bi As Double, ByVal Pamb As Double)
        Me._Type = Type
        Me.Tension = initialTension
        _Tau = timeCste
        _SurfaceTau = surfaceTimeCste
        _A = Ai
        _B = Bi
        _Pamb = Pamb
    End Sub

    ''' <summary>
    ''' Initializes the tissue at surface tension.
    ''' </summary>
    ''' <param name="timeCste"></param>
    ''' <param name="Ai"></param>
    ''' <param name="Bi"></param>
    ''' <param name="Pamb"></param>
    Public Sub New(ByVal Type As Integer, ByVal timeCste As Double,
                    ByVal surfaceTimeCste As Double,
                    ByVal Ai As Double, ByVal Bi As Double, ByVal Pamb As Double)
        Me._Type = Type
        If (_Type = NITROGEN) Then
            Me.Tension = Pamb * FN2inAIR
        Else
            Me.Tension = 0
        End If

        _Tau = timeCste
        _SurfaceTau = surfaceTimeCste
        _A = Ai
        _B = Bi
        _Pamb = Pamb
    End Sub
    Public Function Clone() As BuhlmannSingleTissue
        Return New BuhlmannSingleTissue(_Type, _Tension, _Tau, _SurfaceTau, _A, _B, _Pamb)
    End Function

    ' TENSION CALCULATIONS
    ''' <summary>
    ''' Computes the tissue tension to a next point in time.
    ''' Between the two times:
    ''' - depth can be different (ascending or descending)
    ''' - ascending/descending speed must be CONSTANT
    ''' - gas must be CONSTANT
    ''' - SP (if CC) must be CONSTANT
    ''' </summary>
    ''' <returns></returns>
    Public Function ComputeTension(Gas As Gas, ByVal initialDepth As Double, ByVal finalDepth As Double,
                                    ByVal AscDescRate As Double, ByVal setpoint As Double, ByVal Duration As TimeSpan)
        If (Gas.IsOC) Then
            computeOCtension(Gas, initialDepth, finalDepth, AscDescRate, Duration)
        Else
            computeCCtension(Gas, initialDepth, finalDepth, AscDescRate, setpoint, Duration)
        End If

        ' In some very specific cases, the He level can become negative, causing crazy GF numbers
        ' Setting it back to 0 treats the consequences but not the cause.
        If (_Tension < 0.0) Then
            _Tension = 0
        End If

        Return _Tension
    End Function
    ''' <summary>
    ''' Depth must be sufficient such that SP = PPO2 
    ''' </summary>
    ''' <param name="Gas"></param>
    ''' <param name="initialDepth"></param>
    ''' <param name="finalDepth"></param>
    ''' <param name="AscDescRate"></param>
    ''' <param name="setpoint"></param>
    ''' <param name="Duration"></param>
    Private Sub computeCCtension(Gas As Gas, ByVal initialDepth As Double, ByVal finalDepth As Double,
                                    ByVal AscDescRate As Double, ByVal setpoint As Double, ByVal Duration As TimeSpan)
        ' First verify if the PPO2 will ever be below SET POINT (shallow water) and non constant
        Dim breakingDepth = -1     ' The depth at which the PPO2 switches from Constant to not, or vice versa
        If ((setpoint <> Gas.PO2(finalDepth, setpoint) Or setpoint <> Gas.PO2(initialDepth, setpoint)) And
            Gas.PO2(finalDepth, setpoint) <> Gas.PO2(initialDepth, setpoint)) Then
            breakingDepth = Gas.DepthOfPO2(setpoint, setpoint) ' Get the breakingDepth
        End If

        ' If there is a breakpoint, calculate the durations or part 1 and part 2
        Dim duration1, duration2 As TimeSpan
        If (breakingDepth <> -1) Then
            duration1 = New TimeSpan(System.Math.Abs((breakingDepth - initialDepth) / AscDescRate * 60) * 10000000)
            duration2 = New TimeSpan(System.Math.Abs((breakingDepth - finalDepth) / AscDescRate * 60) * 10000000)
        End If

        ' Check which part is constant PPO2, and which is non-constant PPO2
        If (breakingDepth > initialDepth And breakingDepth > finalDepth) Then
            computeCCRtensionPureOxygen(Duration)   'Breathed mix is pure oxygen
        ElseIf (breakingDepth <> -1 And
            Gas.PO2(finalDepth, setpoint) = setpoint) Then
            computeCCRtensionPureOxygen(duration1)   'Breathed mix is pure oxygen
            ComputeCCtensionSolvedEquation(Gas, breakingDepth, finalDepth, AscDescRate, setpoint, duration2)

        ElseIf (breakingDepth <> -1) Then
            ComputeCCtensionSolvedEquation(Gas, initialDepth, breakingDepth, AscDescRate, setpoint, duration1)
            computeCCRtensionPureOxygen(duration2)   'Breathed mix is pure oxygen
        Else
            ComputeCCtensionSolvedEquation(Gas, initialDepth, finalDepth, AscDescRate, setpoint, Duration)
        End If
    End Sub
    Private Sub ComputeCCtensionSolvedEquation(Gas As Gas, ByVal initialDepth As Double, ByVal finalDepth As Double,
                                    ByVal AscDescRate As Double, ByVal setpoint As Double, ByVal Duration As TimeSpan)
        ' Get the fraction of gas
        Dim gasFraction As Double
        If (_Type = NITROGEN) Then
            gasFraction = Gas.FN2 / (Gas.FN2 + Gas.FHe)
        Else
            gasFraction = Gas.FHe / (Gas.FN2 + Gas.FHe)
        End If

        ' Correct the ascent rate sign if needed
        Dim asc As Double = System.Math.Abs(AscDescRate)
        If (initialDepth - finalDepth > 0) Then
            asc = -asc
        End If

        ' Apply the equation
        _Tension = 33 * ((gasFraction / 33 * ((Duration.TotalMinutes * asc) + initialDepth + (33 * (1 - setpoint)) - (_Tau * asc))) +
                        (Math.exp(-Duration.TotalMinutes / _Tau) *
                        ((_Tension / 33) - (gasFraction / 33 * (initialDepth + (33 * (1 - setpoint)) - (_Tau * asc))))))


    End Sub
    Private Sub computeCCRtensionPureOxygen(ByVal duration As TimeSpan)
        _Tension = _Tension * Math.exp(-duration.TotalMinutes / _Tau)
    End Sub
    Private Sub computeOCtension(Gas As Gas, ByVal initialDepth As Double, ByVal finalDepth As Double,
                                    ByVal AscDescRate As Double, ByVal Duration As TimeSpan)
        ' Get the fraction of gas
        Dim gasFraction As Double
        If (_Type = NITROGEN) Then
            gasFraction = Gas.FN2 / 100.0
        Else
            gasFraction = Gas.FHe / 100.0
        End If

        ' Open Circuit, simply apply the equations for a single slope OC
        Dim asc = System.Math.Abs(AscDescRate)
        If (initialDepth - finalDepth < 0) Then
            asc = -asc
        End If

        _Tension = (gasFraction * ((_Tau * asc) + _Pamb + finalDepth)) +
                    ((_Tension - (gasFraction * (initialDepth + (_Tau * asc) + _Pamb))) *
                    Math.exp(-Duration.TotalMinutes / _Tau))
    End Sub
    Public Sub computeTensionsSurface(ByVal timespan As TimeSpan)
        If (_Type = NITROGEN) Then
            Dim tau As Double = _SurfaceTau
            If (_Tension < _Pamb * FN2inAIR) Then
                tau = _Tau
            End If
            _Tension = (FN2inAIR * _Pamb) + ((_Tension - (FN2inAIR * _Pamb)) * Math.exp(-timespan.TotalMinutes / tau))
        Else
            _Tension = (_Tension * Math.exp(-timespan.TotalMinutes / _SurfaceTau))
        End If
    End Sub

End Class
