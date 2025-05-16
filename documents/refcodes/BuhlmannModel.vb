
<Serializable()>
Public Class BuhlmannModel
    Protected Const MtoFT As Double = 3.2808399
    Protected Const LtoCUFT As Double = 0.03531467

    Private Const CALC_STEP As Double = 1.0             ' step in seconds for numerical resolutions. Smaller step leads to more calculation time
    Public Const MAX_DECOSTOP_TIME = 200                ' Max Time in minutes for each deco stop
    Public Const MAX_CEILING = 500                      ' Value used to avoid an infinity loop when looking for ceiling

    ' Fraction of N2 in Air
    Private Const FN2inAIR = 0.79

    Public Const MIN_ADMISSIBLE_PO2 = 0.17
    Public Const MAX_ADMISSIBLE_PO2 = 1.6

    ' Initial t_xx, t_Sxx, a_xx and b_xx - To be used as constants for initials
    Private t_iN2() As Double = {5.77, 7.22, 11.5, 18.0, 26.7, 39.0, 55.3, 78.4, 111, 157, 211, 270, 345, 440, 563, 719, 916}
    Private t_iSN2() As Double = {86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 86.6, 111, 157, 211, 270, 345, 440, 563, 719, 916}
    Private t_iHe() As Double = {2.18, 2.71, 4.36, 6.81, 10.1, 14.7, 20.9, 29.6, 42.0, 59.5, 79.6, 102, 130, 166, 213, 272, 346}
    Private t_iSHe() As Double = {32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 32.7, 42.0, 59.5, 79.6, 102, 130, 166, 213, 272, 346}
    Private a_iN2() As Double = {41.0, 38.1, 32.6, 28.1, 24.6, 20.2, 16.4, 14.4, 13.0, 11.0, 10.0, 9.1, 8.2, 7.5, 6.8, 6.1, 5.6}
    Private a_iHe() As Double = {56.7, 52.7, 45.0, 38.8, 34.1, 30.0, 26.7, 23.8, 21.2, 19.4, 18.1, 17.4, 16.9, 16.9, 16.9, 16.8, 16.7}
    Private b_iN2() As Double = {0.505, 0.558, 0.651, 0.722, 0.783, 0.813, 0.843, 0.869, 0.891, 0.909, 0.922, 0.932, 0.94, 0.948, 0.954, 0.96, 0.965}
    Private b_iHe() As Double = {0.425, 0.477, 0.575, 0.653, 0.722, 0.758, 0.796, 0.828, 0.855, 0.876, 0.89, 0.9, 0.907, 0.912, 0.917, 0.922, 0.927}

    ' Tissue Loadings
    Protected N2Tissues As List(Of BuhlmannSingleTissue)
    Protected HeTissues As List(Of BuhlmannSingleTissue)
    Public ReadOnly Property TissuesN2 As List(Of BuhlmannSingleTissue)
        Get
            Return N2Tissues
        End Get
    End Property
    Public ReadOnly Property TissuesHe As List(Of BuhlmannSingleTissue)
        Get
            Return HeTissues
        End Get
    End Property

    ' Oxygen Toxicity
    Public OTUD As Double       ' Daily Exposure in Oxygen Toxicity Units
    Public OTUS As Double       ' Single Dive Exposure in Oxygen Toxicity Units
    Public previousDivesOTUD As List(Of Double) = New List(Of Double)       ' List of OTUS values at the end of each previous dives
    Public previousDivesTimes As List(Of TimeSpan) = New List(Of TimeSpan)  ' List of timespans at the end of each previous dives

    ' In ft/min
    Public Shared Function Ar(ByVal Settings As Settings) As Double
        If (Settings Is Nothing) Then
            Return 30
        End If

        If (Settings.Units = Settings.IMPERIAL) Then
            Return 30

        Else
            Return 9 * MtoFT
        End If
    End Function

    ' Surface Calibration Pressure Pamb - in fsw - default value 33 fsw
    'Private Pamb As Double = 33
    Private _Settings As Settings
    Public Property Settings() As Settings
        Get
            Return _Settings
        End Get
        Set(value As Settings)
            _Settings = value
            _Pamb = value.Pamb
            _GFhigh = value.GFhigh
            _GFlow = value.GFlow
        End Set
    End Property

    ' The following values are copied from the settings because it increases execution speed significantly
    Private _Pamb As Double
    Private _GFhigh As Double
    Private _GFlow As Double

    ' Parameters used for the latest update
    Public CurrentGas As Gas
    Public CurrentDepth As Double = 0

    ' Gradient Factor values - Default values 1.00
    Private _DeepestStop As Double = 0
    Private _LockDeepestStop As Boolean = False
    Public Property DeepestStop As Double
        Get
            Return _DeepestStop
        End Get
        Set(value As Double)
            If (Not _LockDeepestStop) Then _DeepestStop = value
        End Set
    End Property
    Public ReadOnly Property GF As Double
        Get
            Return calculateActualCurrentGF()
        End Get
    End Property
    Public Sub LockDeepestStop()
        _LockDeepestStop = True
    End Sub

    ' To be called if no previous dive was done. Assumes all the tissues are at steady states
    ' Pamb given in fsw
    ' TODO: include the GFs
    Public Sub New(ByVal settings As Settings)
        Me.Settings = settings

        OTUS = 0
        OTUD = 0

        ' Initialize the tissues
        N2Tissues = New List(Of BuhlmannSingleTissue)
        HeTissues = New List(Of BuhlmannSingleTissue)
        For i = 0 To 16
            ' Using initial surface tension
            N2Tissues.Add(New BuhlmannSingleTissue(BuhlmannSingleTissue.NITROGEN, t_iN2(i), t_iSN2(i), a_iN2(i), b_iN2(i), _Settings.Pamb))
            HeTissues.Add(New BuhlmannSingleTissue(BuhlmannSingleTissue.HELIUM, t_iHe(i), t_iSHe(i), a_iHe(i), b_iHe(i), _Settings.Pamb))
        Next
    End Sub

    Public Function Clone() As BuhlmannModel
        Dim tis As BuhlmannModel = New BuhlmannModel(_Settings)

        ' Copy the tissues
        For i = 0 To 16
            tis.N2Tissues(i) = Me.N2Tissues(i).Clone()
            tis.HeTissues(i) = Me.HeTissues(i).Clone()
        Next

        tis.CurrentGas = Me.CurrentGas
        tis.CurrentDepth = Me.CurrentDepth
        tis.OTUS = Me.OTUS
        tis.OTUD = Me.OTUD


        For i = 0 To Me.previousDivesOTUD.Count - 1
            tis.previousDivesOTUD.Add(Me.previousDivesOTUD(i))
            tis.previousDivesTimes.Add(Me.previousDivesTimes(i))
        Next

        Return tis
    End Function

    ' Tissues go to a given depth with a given Ascent or Descent Rate.
    ' Tensions are updated
    ' If O2toxicity is true, Oxygen Toxicities are updated
    ' If used in Closed Circuit, checks the actual PPO2 before applying the equations
    ' This function assumes that the AutoSetPoint will not trigger
    Public Sub goToDepth(ByVal Depth As Double, ByVal AscDescRate As Double, ByVal Duration As TimeSpan,
                         ByVal O2toxicity As Boolean, ByVal setpoint As Double)

        For i = 0 To 16
            N2Tissues(i).ComputeTension(CurrentGas, CurrentDepth, Depth, AscDescRate, setpoint, Duration)
            HeTissues(i).ComputeTension(CurrentGas, CurrentDepth, Depth, AscDescRate, setpoint, Duration)
        Next


        ' Oxygen Toxicity
        ' Complicated to solve the Oxygen Toxicity equations literally
        ' So they are applied numerically, with a step of 1 second
        If (O2toxicity) Then

            Dim ta
            If (AscDescRate = 0) Then
                ta = Duration.TotalSeconds / 60.0
            Else
                ta = System.Math.Abs((CurrentDepth - Depth) / AscDescRate)
            End If

            For t = 1 To Int(ta * 60)
                ' t = 0 was already applied previously

                Dim tempDepth = CurrentDepth + (t * AscDescRate / 60)
                Dim ROTD As Double = 0.0, ROTS As Double = 0.0
                Dim po2 = CurrentGas.PO2(tempDepth, setpoint)

                ' Calculate ROTD / ROTS
                If (po2 >= 0.5) Then
                    ROTD = -0.17 + (0.82 * po2) + (0.35 * po2 * po2)
                Else
                    ROTD = 0
                End If

                If (po2 <= 1.0) Then
                    ROTS = ROTD
                ElseIf po2 < 1.13 Then
                    ROTS = -1.5 + (2.5 * po2)
                ElseIf po2 < 1.5 Then
                    ROTS = 4.56 - (7.2 * po2) + (3.84 * po2 * po2)
                Else
                    ROTS = -60 + (41.7 * po2)
                End If

                ' Update the Toxicities
                OTUD = OTUD + (ROTD / 60)
                OTUS = OTUS + (ROTS / 60)
            Next
        End If

        CurrentDepth = Depth
    End Sub

    ' Allows recording the time of the end of the dive and the OTUD level. Used during the surface intervals
    ' to relax OTUD.
    Public Sub TerminateDive(ByVal timeSinceEndOfPreviousDive As TimeSpan)
        previousDivesTimes.Add(timeSinceEndOfPreviousDive)
        previousDivesOTUD.Add(OTUD)
    End Sub

    ' Advances time by timespan, updates and recalculates the tension in every tissue while ON THE SURFACE.
    ' It is assumed the breathed gas is AIR
    Public Sub computeTensionsSurface(ByVal timespan As TimeSpan)
        CurrentDepth = 0
        CurrentGas = New Gas("Imperial", 10, 21, 0, 1.6, False, Drawing.Color.Coral)    ' Air

        For i = 0 To 16
            N2Tissues(i).computeTensionsSurface(timespan)
            HeTissues(i).computeTensionsSurface(timespan)
        Next
    End Sub

    ' Advances time by timespan, updates and recalculates the O2 toxicity in every tissue while ON THE SURFACE.
    ' It is assumed the breathed gas is AIR
    Public Sub computeO2toxicitySurfaceInterval(ByVal surfaceInt As TimeSpan)
        ' Relax OTUS
        'For i = 10 To surfaceInt.TotalMinutes Step 10
        '   OTUS = 0.9259 * OTUS
        'Next
        OTUS = OTUS * Math.exp(-surfaceInt.TotalMinutes / 129.888615)

        ' Relax OTUD from all toxicity accumulated during dives that occured more than 24h before.
        If (TimeSpan.Compare(surfaceInt, New TimeSpan(1, 0, 0, 0)) > 0) Then
            OTUD = 0
            previousDivesOTUD.Clear()
            previousDivesTimes.Clear()

        Else
            Dim time As TimeSpan = surfaceInt
            For i = previousDivesTimes.Count - 1 To 0 Step -1
                time = time.Add(previousDivesTimes(i))
                If (TimeSpan.Compare(time, New TimeSpan(1, 0, 0, 0)) > 0) Then
                    Dim otudDiff As Double = 0
                    If (i > 0) Then otudDiff = previousDivesOTUD(i - 1)
                    ' Delete the older entries
                    For k = 0 To i - 1
                        previousDivesOTUD.RemoveAt(0)
                        previousDivesTimes.RemoveAt(0)
                    Next
                    ' reassign the OTUS minus the difference
                    For k = 0 To previousDivesOTUD.Count - 1
                        previousDivesOTUD(k) = previousDivesOTUD(k) - otudDiff
                    Next
                    ' End
                    Return
                End If
            Next
        End If
    End Sub

    ' Returns TRUE if the Dive doesn't need any deco stop.
    ' FALSE otherwise.
    ' depth in FSW
    ' secondsOffset used to calculate NDL. Leave it to 0 if NDL is not calculated.
    Private Function isNoDecoDive(ByVal gas As Gas, ByVal depth As Double, ByVal secondsOffset As Double,
                                  ByVal setpoint As Double)
        ' Calculate the tensions if the diver goes back to the surface (projection)

        ' Time to ascend to finalDepth - in min
        Dim ta As Double = depth / Ar(_Settings)
        For i = 0 To 16
            ' Make a temporary copy of the tissues
            Dim tmpN2tissue As BuhlmannSingleTissue = N2Tissues(i).Clone()
            Dim tmpHeTissue As BuhlmannSingleTissue = HeTissues(i).Clone()

            ' Project an offset time if we need to calculate the NDL
            If secondsOffset <> 0 Then
                tmpN2tissue.ComputeTension(gas, depth, depth, 0, setpoint, New TimeSpan(secondsOffset * 1000 * 10000))
                tmpHeTissue.ComputeTension(gas, depth, depth, 0, setpoint, New TimeSpan(secondsOffset * 1000 * 10000))
            End If

            ' Compute tensions on the copy of the tissues
            tmpN2tissue.ComputeTension(gas, depth, 0, Ar(_Settings), setpoint, New TimeSpan(ta * 60 * 1000 * 10000)) ' TODO: check that timespan 0 is acceptable !!
            tmpHeTissue.ComputeTension(gas, depth, 0, Ar(_Settings), setpoint, New TimeSpan(ta * 60 * 1000 * 10000))

            ' Calculate Pi, ai and bi
            Dim Pi As Double = tmpN2tissue.Tension + tmpHeTissue.Tension
            Dim ai As Double = ((tmpN2tissue.Tension * tmpN2tissue.A) + (tmpHeTissue.Tension * tmpHeTissue.A)) / Pi
            Dim bi As Double = ((tmpN2tissue.Tension * tmpN2tissue.B) + (tmpHeTissue.Tension * tmpHeTissue.B)) / Pi
            ' Calculate M values - TODO: CONFIRM THIS EQUATION WITH EGI
            Dim M As Double = Mi(0, _GFhigh, ai, bi)
            ' Check if Deco required
            If M < Pi Then
                ' Deco Dive
                Return False
            End If
        Next

        ' No Deco Dive
        Return True
    End Function

    ' TODO: how to include the SP autoswitch to ceiling calculation?
    ' --> TO BE DONE IN DIVEPLAN
    Private Function isCeilingTooHigh(ByVal ceiling As Double, ByVal setpoint As Double)
        ' Time to ascend to finalDepth - in min
        Dim ta = (CurrentDepth - ceiling) / Ar(_Settings)
        Dim maxAuthorizedGFi As Double = getGFmax(ceiling)

        For i = 0 To 16
            ' Make a temporary copy of the tissues
            Dim tmpN2tissue As BuhlmannSingleTissue = N2Tissues(i).Clone()
            Dim tmpHeTissue As BuhlmannSingleTissue = HeTissues(i).Clone()

            ' Compute tensions on the copy of the tissues
            tmpN2tissue.ComputeTension(CurrentGas, CurrentDepth, ceiling, Ar(_Settings), setpoint, New TimeSpan(ta * 60 * 1000 * 10000))
            tmpHeTissue.ComputeTension(CurrentGas, CurrentDepth, ceiling, Ar(_Settings), setpoint, New TimeSpan(ta * 60 * 1000 * 10000))

            Dim actualGFi As Double = tissueGF(tmpN2tissue, tmpHeTissue, ceiling)
            If maxAuthorizedGFi < actualGFi Then
                Return True
            End If
        Next
        Return False
    End Function
    Public Function isNoDecoDive(ByVal setpoint As Double)
        Return isNoDecoDive(CurrentGas, CurrentDepth, 0, setpoint)
    End Function

    ' Return the NDT as a TimeSpan
    Public Function getNoDecoLimit()
        ' Note: in order to improve performance, the time is searched using centers methods.
        ' First, 0min is checked. Then MAX_DECOSTOP_TIME/2, then /2 and so on...

        ' First, check 0min noDecoTime
        If (Not isNoDecoDive(CurrentGas, CurrentDepth, 0, 0)) Then
            Return New TimeSpan(0)
        End If

        ' Then, check max time
        If (isNoDecoDive(CurrentGas, CurrentDepth, MAX_DECOSTOP_TIME * 60, 0)) Then
            Return New TimeSpan(0, MAX_DECOSTOP_TIME - 1, 0)
        End If

        ' Investigate centers until finding the limit
        Dim limitFound As Boolean = False
        Dim increment As Integer = Int(MAX_DECOSTOP_TIME / 4)
        Dim timeLimit As Integer = Int(MAX_DECOSTOP_TIME / 2)

        While Not limitFound And increment > 4
            If (isNoDecoDive(CurrentGas, CurrentDepth, 60 * timeLimit, 0)) Then
                timeLimit += increment
            Else
                timeLimit -= increment
            End If
            increment /= 2
        End While

        ' Finally, calculate every minute
        If (isNoDecoDive(CurrentGas, CurrentDepth, 60 * timeLimit, 0)) Then
            timeLimit += 1
            While isNoDecoDive(CurrentGas, CurrentDepth, timeLimit * 60, 0) And timeLimit < MAX_DECOSTOP_TIME
                timeLimit += 1
            End While
            Return New TimeSpan(0, timeLimit - 1, 0)
        Else
            timeLimit -= 1
            While (Not isNoDecoDive(CurrentGas, CurrentDepth, timeLimit * 60, 0)) And timeLimit > 0
                timeLimit -= 1
            End While
            Return New TimeSpan(0, timeLimit, 0)
        End If

    End Function

    Public Function getCeiling() As Double
        Return getCeiling(0)
    End Function
    Public Function getCeiling(ByVal setpoint As Double) As Double
        ' If not need to deco, Ceiling is 0 fsw / 0 m
        If isNoDecoDive(0) Then
            Return 0
        End If

        ' DecoStop depth
        Dim ceiling As Double = 10

        If (_Settings.Units = Settings.IMPERIAL) Then

            While (isCeilingTooHigh(ceiling, setpoint) And ceiling < MAX_CEILING)
                ceiling = ceiling + 10
            End While

            Return ceiling
        Else
            ceiling = 3 * MtoFT
            While (isCeilingTooHigh(ceiling, setpoint) And ceiling < MAX_CEILING)
                ceiling = ceiling + (3 * MtoFT)
            End While

            Return ceiling
        End If
    End Function

    ' Returns decoTime in minutes
    ' nextAscent is the depth difference between current depth and next decostop
    ' Typically 10 ft / 3 meter, except with the last deco stop, which can be 20 ft / 6m
    ' If returns 200 min, then there was no solution found within 199 min: ERROR
    Public Function calculateDecoTime(ByVal nextAscent As Double, ByVal setpoint As Double)
        Dim time = 0    ' Time in minutes
        While isDecoTimeTooShort(time, nextAscent, setpoint) And time < MAX_DECOSTOP_TIME
            time = time + 1
        End While

        Return time
    End Function

    ' tDeco in Minute
    Private Function isDecoTimeTooShort(ByVal tDeco As Double, ByVal nextAscent As Double, ByVal setpoint As Double)
        ' Time to ascend to finalDepth - in min
        Dim ta As Double = (nextAscent) / Ar(_Settings)
        Dim maxAuthorizedGFi As Double = getGFmax(CurrentDepth - nextAscent)

        For i = 0 To 16
            ' Make a temporary copy of the tissues
            Dim tmpN2tissue As BuhlmannSingleTissue = N2Tissues(i).Clone()
            Dim tmpHeTissue As BuhlmannSingleTissue = HeTissues(i).Clone()

            ' Project an offset time if we need to calculate the NDL
            If tDeco <> 0 Then
                tmpN2tissue.ComputeTension(CurrentGas, CurrentDepth, CurrentDepth, 0, setpoint, New TimeSpan(tDeco * 60 * 1000 * 10000))
                tmpHeTissue.ComputeTension(CurrentGas, CurrentDepth, CurrentDepth, 0, setpoint, New TimeSpan(tDeco * 60 * 1000 * 10000))
            End If

            ' Compute tensions on the copy of the tissues
            tmpN2tissue.ComputeTension(CurrentGas, CurrentDepth, CurrentDepth - nextAscent, Ar(_Settings), setpoint, New TimeSpan(ta * 60 * 1000 * 10000)) ' TODO: check that timespan 0 is acceptable !!
            tmpHeTissue.ComputeTension(CurrentGas, CurrentDepth, CurrentDepth - nextAscent, Ar(_Settings), setpoint, New TimeSpan(ta * 60 * 1000 * 10000))

            Dim actualGFi As Double = tissueGF(tmpN2tissue, tmpHeTissue, CurrentDepth - nextAscent)

            ' Check if Deco required
            If maxAuthorizedGFi < actualGFi Then
                Return True
            End If
        Next
        Return False
    End Function

    Public Function calculateActualCurrentGF() As Double
        ' See definition in Page 15 - Gradient Factor
        Dim maxGFi As Double = 0
        For i = 0 To 16
            Dim GFi As Double = tissueGF(N2Tissues(i), HeTissues(i), CurrentDepth)
            If (i = 0) Then
                maxGFi = GFi
            ElseIf (GFi > maxGFi) Then
                maxGFi = GFi
            End If
        Next
        ' No Deco Dive
        Return (maxGFi * 100.0)
    End Function

    Private Function tissueGF(ByVal N2tissue As BuhlmannSingleTissue,
                              ByVal HeTissue As BuhlmannSingleTissue,
                              ByVal depth As Double) As Double
        ' Calculate Pi, ai and bi
        Dim Pi As Double = N2tissue.Tension + HeTissue.Tension
        Dim ai As Double = ((N2tissue.Tension * N2tissue.A) + (HeTissue.Tension * HeTissue.A)) / Pi
        Dim bi As Double = ((N2tissue.Tension * N2tissue.B) + (HeTissue.Tension * HeTissue.B)) / Pi
        Dim Mi As Double = ai + ((depth + _Pamb) / bi)

        ' Calculate M values
        Dim GFi As Double = (Pi - depth - _Pamb) / (Mi - depth - _Pamb)

        Return GFi
    End Function

    ' Return the admissible GF value for a given depth
    ' Can be called ONLY after the DeepestStop has been set (calculated with function getCeiling() )
    Private Function getGFmax(ByVal depth As Double) As Double
        If (DeepestStop = 0 Or CurrentDepth > DeepestStop) Then
            Return _GFlow
        End If
        Return _GFlow + ((_GFhigh - _GFlow) * ((DeepestStop - depth) / DeepestStop))
    End Function
    Private Function Mi(ByVal depth As Double, ByVal maxAuthorizedGF As Double,
                       ByVal ai As Double, ByVal bi As Double) As Double
        Dim M = depth + _Pamb + (maxAuthorizedGF * (ai + ((_Pamb + depth) * ((1 / bi) - 1))))
        Return M
    End Function
    Public Sub changeGas(ByVal gas As Gas)
        CurrentGas = gas
    End Sub

End Class
