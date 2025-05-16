Imports System.ComponentModel
Imports HollisTrimixAlgo


<Serializable()>
Public Class DivePlan

    ' Preprocessor Directives for compilation
#Const PrintDetailsGF = 0

    ' Constants defining the state of the dive plan
    Public Const RESET As Integer = 0
    Public Const CALCULATINGSHORT As Integer = 1
    Public Const SHORTCALCULATED As Integer = 2
    Public Const CALCULATINGDETAILS As Integer = 3
    Public Const DETAILSCALCULATED As Integer = 4
    Public Const FAILEDCALCULATION_DECOTOOLONG As Integer = 5
    Public Const CANCELEDCALCULATION As Integer = 6
    Public Const LOSTGASCALCULATED As Integer = 7
    Private _State As Integer = RESET

    Public Const FIXEDSTEP As Double = 5.0   ' For Dev only. Fixed time jump in DetailedOutput

    Protected Const MtoFT As Double = 3.2808399
    Protected Const LtoCUFT As Double = 0.03531467

    Public buhlModel As BuhlmannModel

    Public SummaryPlan As Summary

    ' Inputs
    Public _GasList As Gas()
    Public _LegsList As Leg()
    Public _Settings As Settings
    Public _SurfaceInterval As TimeSpan
    Public _PreviousDivePlan As DivePlan    ' Important in order to calculate the surface interval tissues tensions

    ' Outputs
    Private _DivePoints As List(Of DivePoint)
    Public _LowDetailedOutputDivePoints As List(Of DivePoint)
    Public _OutputMaxTimeSpan
    Public _DetailedOutputDivePoints As List(Of DivePoint)
    Public MaxDepth As Double
    Public MaxTimeSpan As TimeSpan
    Public PO2max As Double
    Public PO2min As Double
    Public ENDmax As Double
    Public WOBmax As Double
    Public Result As DPResults = New DPResults()
    Public FirstDecoStopDepth As Double = 0.0

    ' Marker, since the auto switch can be triggered only once
    Protected AutoSwitchHighTriggered As Boolean = False
    Protected AutoSwitchLowTriggered As Boolean = False

    ' Legs max time: used to determine whether to use RMVDive or RMVDeco
    Public LegsMaxTime As TimeSpan

    ' Worker to calculate the plan
    <NonSerialized>
    Private WithEvents BackgroundWorkerCalculatePlan As BackgroundWorker
    Private CalculatePlanOperationWaiting = False

    ' Used to calculate the TAT quickly during ascent of Detailed Output
    Private _LastTAT As TimeSpan = New TimeSpan(0)

    ' Between 0 and 100 (%) - To be read by the registered app when the PlanDetailsCalculationStateUpdate event is fired
    Public CalculationState As Integer = 0

    ' Variables used to determine whether an EventHandler was added or not
    Public DivePlanReadyHandlerAdded As Boolean = False
    Public DivePlanDetailsReadyHandlerAdded As Boolean = False
    Public DivePlanUpdateHandlerAdded As Boolean = False

    ' Depth of the failed deco stop (because too long)
    Public FailedDecoStop As Double = 0.0

    ' Used only for debug purpose. Contains details on the GF and tensions calculations
#If PrintDetailsGF = 1 Then
    Private GFDetailsTextOutput As String = ""
#End If

    Public ReadOnly Property State As Integer
        Get
            Return _State
        End Get
    End Property
    Private Property PState
        Get
            Return _State
        End Get
        Set(value)
            _State = value

            ' Trigger event
            OnPlanStateChanged()
        End Set
    End Property

    Public Property GasListArray() As Gas()
        Get
            Return _GasList
        End Get
        Set(value As Gas())
            _GasList = value
        End Set
    End Property
    Public ReadOnly Property GasList As List(Of Gas)
        Get
            Dim list As List(Of Gas) = New List(Of Gas)
            For Each gas In _GasList
                list.Add(gas)
            Next
            Return list
        End Get
    End Property


    Public Property LegsList() As Leg()
        Get
            Return _LegsList
        End Get
        Set(value As Leg())
            _LegsList = value
        End Set
    End Property

    Public Property Legs As List(Of Leg)
        Get
            If (_LegsList IsNot Nothing) Then
                If (_LegsList.Length > 0) Then
                    Dim ls As List(Of Leg) = New List(Of Leg)
                    For Each l In _LegsList
                        ls.Add(l)
                    Next
                    Return ls
                End If
            End If
            Return New List(Of Leg)
        End Get
        Set(value As List(Of Leg))
            Dim legs(value.Count - 1) As Leg
            For i = 0 To value.Count - 1
                legs(i) = value(i)
            Next
            _LegsList = legs
        End Set
    End Property
    Public Property DivePoints() As List(Of DivePoint)
        Get
            Return _DivePoints
        End Get
        Set(value As List(Of DivePoint))
            _DivePoints = value
        End Set
    End Property

    ' To be called ONLY if the diveplan was properly calculated
    Public ReadOnly Property TotalAscentTime As TimeSpan
        Get
            If (_LegsList Is Nothing) Then Return New TimeSpan(0)
            Dim tp As TimeSpan = New TimeSpan(0)
            For Each leg In _LegsList
                tp = tp.Add(leg.Time)
            Next

            Return DivePoints(DivePoints.Count - 1).Time.Subtract(tp)
        End Get
    End Property


    Public Sub New(ByVal previousDivePlan As DivePlan, ByVal settings As Settings, ByVal gaslist As Gas(), ByVal legslist As Leg())
        ' In order to execute the plan calculation in background
        BackgroundWorkerCalculatePlan = New BackgroundWorker()
        BackgroundWorkerCalculatePlan.WorkerReportsProgress = True
        BackgroundWorkerCalculatePlan.WorkerSupportsCancellation = True
        AddHandler BackgroundWorkerCalculatePlan.ProgressChanged, AddressOf BackgroundWorkerCalculatePlan_ReportProgress
        AddHandler BackgroundWorkerCalculatePlan.DoWork, AddressOf BackgroundWorkerCalculatePlan_DoWork
        AddHandler BackgroundWorkerCalculatePlan.RunWorkerCompleted, AddressOf BackgroundWorkerCalculatePlan_Completed

        _PreviousDivePlan = previousDivePlan
        _Settings = settings
        _GasList = gaslist
        _LegsList = legslist

        _DivePoints = New List(Of DivePoint)
        _DetailedOutputDivePoints = New List(Of DivePoint)

        PState = RESET
    End Sub

    Public Sub New(ByVal previousDivePlan As DivePlan, ByVal settings As Settings, ByVal gaslist As Gas(), ByVal leglist As List(Of Leg))
        ' Create the Legs array
        Dim legs(leglist.Count - 1) As Leg
        For i = 0 To legs.Count - 1
            legs(i) = leglist(i)
        Next

        ' In order to execute the plan calculation in background
        BackgroundWorkerCalculatePlan = New BackgroundWorker()
        BackgroundWorkerCalculatePlan.WorkerReportsProgress = True
        BackgroundWorkerCalculatePlan.WorkerSupportsCancellation = True
        AddHandler BackgroundWorkerCalculatePlan.ProgressChanged, AddressOf BackgroundWorkerCalculatePlan_ReportProgress
        AddHandler BackgroundWorkerCalculatePlan.DoWork, AddressOf BackgroundWorkerCalculatePlan_DoWork
        AddHandler BackgroundWorkerCalculatePlan.RunWorkerCompleted, AddressOf BackgroundWorkerCalculatePlan_Completed

        _PreviousDivePlan = previousDivePlan
        _Settings = settings
        _GasList = gaslist
        _LegsList = legs

        _DivePoints = New List(Of DivePoint)
        _DetailedOutputDivePoints = New List(Of DivePoint)

        PState = RESET
    End Sub

    Public Sub New(ByVal previousDivePlan As DivePlan, ByVal settings As Settings, ByVal gaslist As List(Of Gas), ByVal leglist As List(Of Leg))
        ' Create the Legs array
        Dim gases(gaslist.Count - 1) As Gas
        For i = 0 To gases.Count - 1
            gases(i) = gaslist(i)
        Next

        ' Create the Legs array
        Dim legs(leglist.Count - 1) As Leg
        For i = 0 To legs.Count - 1
            legs(i) = leglist(i)
        Next

        ' In order to execute the plan calculation in background
        BackgroundWorkerCalculatePlan = New BackgroundWorker()
        BackgroundWorkerCalculatePlan.WorkerReportsProgress = True
        BackgroundWorkerCalculatePlan.WorkerSupportsCancellation = True
        AddHandler BackgroundWorkerCalculatePlan.ProgressChanged, AddressOf BackgroundWorkerCalculatePlan_ReportProgress
        AddHandler BackgroundWorkerCalculatePlan.DoWork, AddressOf BackgroundWorkerCalculatePlan_DoWork
        AddHandler BackgroundWorkerCalculatePlan.RunWorkerCompleted, AddressOf BackgroundWorkerCalculatePlan_Completed

        _PreviousDivePlan = previousDivePlan
        _Settings = settings
        _GasList = gases
        _LegsList = legs

        _DivePoints = New List(Of DivePoint)
        _DetailedOutputDivePoints = New List(Of DivePoint)

        PState = RESET
    End Sub

    Public Sub ResetDivePlan(ByVal settings As Settings, ByVal gaslist As Gas(), ByVal legslist As Leg())
        ' Cancel the currently running background workers, if any
        If (BackgroundWorkerCalculatePlan.IsBusy) Then
            BackgroundWorkerCalculatePlan.CancelAsync()
        End If

        PState = RESET
        _Settings = settings
        _GasList = gaslist
        _LegsList = legslist
        _DivePoints = New List(Of DivePoint)()
        _DetailedOutputDivePoints = New List(Of DivePoint)()

        If _LegsList IsNot Nothing Then
            calculateDivePlan()
        End If
    End Sub

    Public Sub calculateDivePlan()
        ' Cancel the currently running background workers, if any
        If (BackgroundWorkerCalculatePlan.IsBusy) Then
            CalculatePlanOperationWaiting = True        ' Notifies that the worker was cancelled and must be run again
            BackgroundWorkerCalculatePlan.CancelAsync()
            Return
        End If

        ' Finally, run the thread in background
        BackgroundWorkerCalculatePlan.RunWorkerAsync()

    End Sub

    ' Creates the DivePoints from the given Legs but doesn't calculate the deco
    ' Calculates the tensions up to the last point of the last leg
    Public Sub modeliseLegs()
        ' CALCULATE THE DIVEPOINTS
        _DivePoints = New List(Of DivePoint)
        _DetailedOutputDivePoints = New List(Of DivePoint)
        buhlModel = New BuhlmannModel(_Settings)
        MaxTimeSpan = New TimeSpan(0, 0, 0)

        ' Nothing to calculate if there is no leg
        If (_LegsList.Count = 0) Then
            Return
        End If

        ' Experimental here
        ComputeSurfaceInterval()

        BuildPointsFromLegs(False, True, True, True, True)
    End Sub

    Public Function getGasByID(ByVal gas As Gas) As Gas
        For i = 0 To 5
            If (_GasList(i).ID = gas.ID) Then
                Return _GasList(i)
            End If
        Next

        Return _GasList(0)
    End Function

    ' Calculates the tension and O2 toxicity in the tissues during the surface interval
    ' between the _PreviousDivePlan and the beginning of the dive.
    Private Sub ComputeSurfaceInterval()
        If (_PreviousDivePlan Is Nothing) Then Return

        ' Copy the tissues
        Dim tis As BuhlmannModel = _PreviousDivePlan.buhlModel.Clone()

        ' Tensions
        tis.computeTensionsSurface(_SurfaceInterval)

        ' O2 Toxicity
        tis.computeO2toxicitySurfaceInterval(_SurfaceInterval)

        ' Copy to the tissues
        buhlModel = tis
    End Sub

    ' First part of the dive plan, simply based on the input legs
    Private Sub BuildPointsFromLegs(ByVal e As System.ComponentModel.DoWorkEventArgs,
                                    ByVal gasConsum As Boolean)

        BuildPointsFromLegs(True, False, False, gasConsum, False)

        If BackgroundWorkerCalculatePlan.CancellationPending Then
            e.Cancel = True
            Return
        End If
    End Sub

    Private Sub BuildPointsFromLegs(ByVal backgroundWorker,
                                   ByVal modelisingLegs, ByVal O2tox,
                                     ByVal gasConsum, ByVal details)
        ' Assign the gas
        Dim travelGas As Gas = _GasList(0)
        If _LegsList.Count > 0 Then travelGas = _LegsList(0).Gas

        ' Initialize
        AutoSwitchHighTriggered = False
        AutoSwitchLowTriggered = False
        Dim currentTime As TimeSpan = New TimeSpan(0)
        Dim DivePoint = New DivePoint(_Settings, currentTime, 0, travelGas)
        ' For the set point in case the leg is Closed Circuit
        If (_LegsList.Count > 0) Then
            Dim sp = 0.0
            If (_LegsList(0).IsCC) Then sp = _LegsList(0).SetPoint
            DivePoint.SetPoint = sp
        End If
        _DivePoints.Add(DivePoint)
        buhlModel.changeGas(_DivePoints(0).Gas)
        Me.useGas(_DivePoints(0).Gas)
        Me.MaxDepth = 0

        Dim previousLeg = Nothing
        For Each leg As Leg In _LegsList
            ' Check if need to cancel the thread
            If (backgroundWorker And
                BackgroundWorkerCalculatePlan.CancellationPending) Then
                Return
            End If

            ' Calculate the times of ascent and bottom
            Dim ascDescTime As TimeSpan = New TimeSpan(0)
            If leg.Depth <> _DivePoints.Last.Depth Then
                Dim depthDiff As Double = leg.Depth - _DivePoints.Last.Depth
                Dim timeMin As Double
                If depthDiff > 0 Then
                    timeMin = depthDiff / leg.DescentRate
                Else
                    timeMin = -depthDiff / leg.AscentRate
                End If
                ' Round to the nearest second
                ascDescTime = New TimeSpan(System.Math.Round(timeMin * 60) * 1000 * 10000)
            End If
            Dim bottomTime As TimeSpan = leg.Time.Subtract(ascDescTime)

            ' For the set point in case the leg is Closed Circuit
            Dim sp As Double = _DivePoints.Last.SetPoint
            If (leg.IsCC) Then
                sp = leg.SetPoint
            End If

            ' Record the time of the defined legs, before starting to calculate the ascent
            LegsMaxTime = _DivePoints.Last.Time.Add(bottomTime.Add(ascDescTime))

            ' Add a point with previous gas (travel gas)
            LegGoTo(leg.Depth, leg.AscentRate, leg.DescentRate, gasConsum, sp)

            ' If the gas was changed, then apply the ExtSwitchMixStop
            If (buhlModel.CurrentGas.ID <> leg.Gas.ID And _Settings.ExtendedMixSwitchStop.TotalMinutes > 0) Then
                MaxTimeSpan = _DivePoints.Last.Time ' TODO: get rid of that
                StayForDeco(buhlModel.CurrentGas, _Settings.ExtendedMixSwitchStop, gasConsum, O2tox)
            End If

            ' Since the gas was changed, verify that if OC to CC, set the correct SP
            If (buhlModel.CurrentGas.IsOC And leg.Gas.IsCC) Then
                sp = leg.SetPoint
            End If

            ' Add a point at the same time/depth but with leg's gas
            StayForDeco(leg.Gas, bottomTime, gasConsum, O2tox)

            previousLeg = leg
        Next



    End Sub

    <Obsolete("This method is deprecated and was completely re-written. Used BuildPointsFromLegs instead.")>
    Private Sub BuildPointsFromLegs2(ByVal backgroundWorker,
                                   ByVal modelisingLegs, ByVal O2tox, ByVal gasConsum, ByVal details)
        ' Marker, since the auto switch can be triggered only once
        AutoSwitchHighTriggered = False
        AutoSwitchLowTriggered = False
        Dim currentDepth As Double = 0
        Dim currentTime As TimeSpan = New TimeSpan(0)
        Dim DivePoint = New DivePoint(_Settings, currentTime, currentDepth, _LegsList(0).Gas)

        ' For the set point in case the leg is Closed Circuit
        If (_LegsList.Count > 0) Then
            Dim sp = 0.0
            If (_LegsList(0).IsCC) Then sp = _LegsList(0).SetPoint
            DivePoint.SetPoint = sp
        End If

        _DivePoints.Add(DivePoint)
        Me.MaxDepth = currentDepth

        ' Calculate the total time of legs
        Dim totalLegsTime = New TimeSpan(0)
        For Each leg In _LegsList
            totalLegsTime = totalLegsTime.Add(leg.Time)
        Next

        ' Rule changed: now the gas is changed once the diver has reached the leg depth, not before
        ' ascending or descending, except for the first leg.
        Dim travelGas As Gas = _GasList(0)
        If _LegsList.Count > 0 Then travelGas = _LegsList(0).Gas
        Dim previousLeg = Nothing
        For Each leg As Leg In _LegsList
            ' Check if need to cancel the thread
            If (backgroundWorker And
                BackgroundWorkerCalculatePlan.CancellationPending) Then
                Return
            End If

            Dim ascDescTime As TimeSpan = New TimeSpan(0)
            If leg.Depth <> currentDepth Then
                Dim depthDiff As Double = leg.Depth - currentDepth
                Dim timeMin As Double
                If depthDiff > 0 Then
                    timeMin = depthDiff / leg.DescentRate
                Else
                    timeMin = -depthDiff / leg.AscentRate
                End If
                ' Round to the nearest second
                ascDescTime = New TimeSpan(System.Math.Round(timeMin * 60) * 1000 * 10000)
            End If

            ' For the set point in case the leg is Closed Circuit
            Dim sp = 1.0
            If (previousLeg IsNot Nothing) Then
                sp = previousLeg.SetPoint
            ElseIf (leg.IsCC) Then
                sp = leg.SetPoint
            End If
            If (_DivePoints.Count > 0) Then sp = _DivePoints.Last.SetPoint

            ' Set point corresponds to the leg settings
            If (leg.IsCC) Then
                sp = leg.SetPoint
            End If

            _DivePoints(_DivePoints.Count - 1).SetPoint = sp
            Dim currentTimeTemp = currentTime.Add(ascDescTime)
            currentDepth = leg.Depth

            ' Used to properly calculate the gas consumption
            LegsMaxTime = totalLegsTime

            ' Add a point with previous gas (travel gas)
            DivePoint = New DivePoint(_Settings, currentTimeTemp, currentDepth, travelGas)
            DivePoint.SetPoint = sp
            addDivePointToPlan(buhlModel, _DivePoints, DivePoint, False, O2tox, gasConsum, False, travelGas.IsCC, sp, False)

            ' If the gas was changed, then apply the ExtSwitchMixStop
            If (travelGas.ID <> leg.Gas.ID) Then
                ' If using Extended Mix Switch Stop, then stop
                If (_Settings.ExtendedMixSwitchStop.TotalMinutes > 0) Then
                    MaxTimeSpan = _DivePoints.Last.Time
                    StayForDeco(travelGas, _Settings.ExtendedMixSwitchStop, gasConsum, O2tox)
                    currentTimeTemp = currentTimeTemp.Add(_Settings.ExtendedMixSwitchStop)
                    currentTime = currentTime.Add(_Settings.ExtendedMixSwitchStop)
                End If
            End If

            ' Since the gas was changed, verify that if OC to CC, set the correct SP
            If (travelGas.IsOC And leg.Gas.IsCC) Then
                sp = leg.SetPoint
            End If
            travelGas = leg.Gas

            ' Add a point at the same time/depth but with leg's gas
            DivePoint = New DivePoint(_Settings, currentTimeTemp, currentDepth, leg.Gas)
            addDivePointToPlan(buhlModel, _DivePoints, DivePoint, False, O2tox, gasConsum, False, leg.IsCC, sp, False)

            ' Add a point at the end of the stop (same depth, different time)
            currentTime = currentTime.Add(leg.Time)    ' take into account the ascent or descent time
            DivePoint = New DivePoint(_Settings, currentTime, currentDepth, leg.Gas)
            sp = _DivePoints(_DivePoints.Count - 1).SetPoint    ' In case the autoswitch was activated, copy the last point's set point
            addDivePointToPlan(buhlModel, _DivePoints, DivePoint, False, O2tox, gasConsum, False, leg.IsCC, sp, False)

            ' Update MaxTimeSpan
            MaxTimeSpan = _DivePoints(_DivePoints.Count - 1).Time

            If Me.MaxDepth < leg.Depth Then
                Me.MaxDepth = leg.Depth
            End If

            ' Record the time of the defined legs, before starting to calculate the ascent
            LegsMaxTime = MaxTimeSpan

            previousLeg = leg
        Next
    End Sub

    ' Complete rewrite of the function
    Private Sub BuildDecoPoints(ByVal e As System.ComponentModel.DoWorkEventArgs,
                                    ByVal gasConsum As Boolean)
        If BackgroundWorkerCalculatePlan.CancellationPending Then
            Result.Result = DPResults.CANCELED
            PState = CANCELEDCALCULATION
            e.Cancel = True
            Return
        End If


        ' CHECK IF NO DECO IS POSSIBLE
        MaxTimeSpan = DivePoints.Last.Time
        If isNoDecoDive() Then
            DecoAscendTo(0, False, False, gasConsum)
            ' End of the No Deco Dive
        Else
            ' Build Deco Stops
            Dim currentDepth As Double = DivePoints.Last.Depth
            Dim currentTime As TimeSpan = DivePoints.Last.Time
            Dim currentGas As Gas = DivePoints.Last.Gas
            buhlModel.Settings = _Settings
            FirstDecoStopDepth = 0

            ' The best gas can be changed DURING the FIRST ascent (to the deepest stop).
            ' Then, all gas must be changed at the beginning of the deco stop.
            ' Step 1: calculate ceiling with current gas
            ' Step 2: check best gas at ceiling (rounded up 10 ft)
            ' Step 3: if best gas at ceiling different, calculate by iteration the depth
            ' at which the best gas is different from initial gas. Go there and return to Step 2
            ' Step 4: if best gas at ceiling unchanged, go there and calculate decotime

            ' SELECT BEST GAS (Highest PO2)
            Dim usedGas = selectBestDecoGas(currentDepth, currentGas)
            buhlModel.changeGas(usedGas)
            useGas(usedGas)

            ' Step 1: Calculate DeepestStop
            Dim DeepestStop = ceiling()

            ' If the deepest stop is shallower than the LastStop set in the settings, then adjust to the laststop level
            If (_Settings.LastStop > 12 And DeepestStop < _Settings.LastStop) Then
                DeepestStop = _Settings.LastStop
            End If

            ' Mark the first (deepest) deco stop
            buhlModel.DeepestStop = DeepestStop
            FirstDecoStopDepth = DeepestStop

            ' Start Ascending
            Dim targetDepth As Double = DeepestStop
            While targetDepth >= 0
                If BackgroundWorkerCalculatePlan.CancellationPending Then
                    Result.Result = DPResults.CANCELED
                    PState = CANCELEDCALCULATION
                    e.Cancel = True
                    Return
                End If

                ' Ascend to the targetDepth
                DecoAscendTo(targetDepth, False, False, gasConsum)

                ' Change the gas if better gas available
                If (targetDepth > 0) Then ChangeGasIfAnyBetter(gasConsum)

                ' Do the deco stop
                If targetDepth > 0 Then
                    Dim time As TimeSpan = decoTime(targetDepth, DivePoints.Last.SetPoint)

                    ' If no solution found, stop calculating the dive plan
                    If (time.TotalMinutes >= BuhlmannModel.MAX_DECOSTOP_TIME) Then
                        Me.FailedDecoStop = targetDepth
                        Me.PState = FAILEDCALCULATION_DECOTOOLONG
                        Return
                    End If

                    ' The go to the point
                    StayForDeco(buhlModel.CurrentGas, time, gasConsum, False)

                    ' Update MaxTimeSpan
                    MaxTimeSpan = DivePoints.Last.Time
                End If

                ' Ascend to the next stop
                If (_Settings.LastStop > targetDepth - _Settings.decoStep()) Then
                    targetDepth = targetDepth - _Settings.LastStop
                Else
                    targetDepth -= _Settings.decoStep()

                    If (targetDepth < _Settings.LastStop) Then
                        targetDepth = _Settings.LastStop
                    End If
                End If
                ' Correct the approximation due to repetitive addition on doubles (order of E-12)
                If (System.Math.Abs(targetDepth) < 1.0) Then targetDepth = 0
            End While
        End If
    End Sub

    ' To be called at the beginning of a deco, after switching to the better mix.
    ' Returns the deco time at the given depth in minutes
    Private Function decoTime(ByVal depth As Double, ByVal setpoint As Double) As TimeSpan
        If (buhlModel.CurrentGas.IsCC And _Settings.AutoSwitchToLowEnabled And
            depth >= _Settings.AutoSwitchToLow And depth - _Settings.decoStep <= _Settings.AutoSwitchToLow) Then
            ' This function greatly impacts performance.
            ' Use it only when necessary
            Return decoTimeCCwithSPSwitch(depth, setpoint)
        Else
            ' Faster execution, simpler conditions
            Return decoTimeFastCalculation(depth, setpoint)
        End If
    End Function

    Private Function decoTimeFastCalculation(ByVal depth As Double, ByVal setpoint As Double) As TimeSpan
        Dim time As Integer
        If (_Settings.LastStop > depth - _Settings.decoStep) Then
            time = buhlModel.calculateDecoTime(depth, setpoint)
        Else
            time = buhlModel.calculateDecoTime(_Settings.decoStep, setpoint)
        End If
        If (time = 0) Then time = 1
        Return New TimeSpan(0, time, 0)
    End Function

    Private Function decoTimeCCwithSPSwitch(ByVal depth As Double, ByVal setpoint As Double) As TimeSpan
        Dim time As Integer = 0
        Dim correctTime As Boolean = False

        Dim targetDepth = depth - _Settings.decoStep
        If (_Settings.LastStop > depth - _Settings.decoStep) Then targetDepth = 0

        Do
            time += 1
            Dim dp As DivePlan = Me.CloneCurrentState()
            dp.StayForDeco(dp.buhlModel.CurrentGas, New TimeSpan(0, time, 0), False, False)
            dp.DecoAscendTo(targetDepth, True, False, False)

            correctTime = dp.isGFrespected(False)
        Loop Until correctTime Or time >= BuhlmannModel.MAX_DECOSTOP_TIME


        'If (_Settings.LastStop > depth - _Settings.decoStep) Then
        '    time = buhlModel.calculateDecoTime(depth, setpoint)
        'Else
        '    time = buhlModel.calculateDecoTime(_Settings.decoStep, setpoint)
        'End If
        'If (time = 0) Then time = 1
        Return New TimeSpan(0, time, 0)
    End Function

    Private Sub GetMinMaxPo2WobEnd()
        Dim i = 0
        For Each p In _DivePoints
            If (i = 0) Then
                PO2max = p.Gas.PO2(p.Depth)
                PO2min = p.Gas.PO2(p.Depth)
                ENDmax = p.Gas.ENDa(p.Depth, _Settings)
                WOBmax = p.Gas.WOB(p.Depth)
            Else
                If (p.Gas.PO2(p.Depth) > PO2max) Then
                    PO2max = p.Gas.PO2(p.Depth)
                End If
                If (p.Gas.PO2(p.Depth) < PO2min) Then
                    PO2min = p.Gas.PO2(p.Depth)
                End If
                If (p.Gas.ENDa(p.Depth, _Settings) > ENDmax) Then
                    ENDmax = p.Gas.ENDa(p.Depth, _Settings)
                End If
                If WOBmax < p.Gas.WOB(p.Depth) Then
                    WOBmax = p.Gas.WOB(p.Depth)
                End If
            End If
            i = i + 1
        Next
    End Sub

    Private Sub GenerateDetailedOutput(ByVal e As System.ComponentModel.DoWorkEventArgs)
#If PrintDetailsGF = 1 Then
        GFDetailsTextOutput = ""
#End If

        ' GENERATE DETAILED OUTPUT FOR THE THREAD
        If _DetailedOutputDivePoints IsNot Nothing Then
            _DetailedOutputDivePoints.Clear()
        End If

        ' Estimate the calculation time / length
        Dim totalTimeSec = _DivePoints(_DivePoints.Count - 1).Time.TotalSeconds
        Dim percentage As Integer = 0

        ' Re-Initialize the Gases' Capacities
        For Each gas In _GasList
            gas.ResetCapacity()
        Next

        ' For better performance, initialize the list length
        _DetailedOutputDivePoints = New List(Of DivePoint)(totalTimeSec + 100)
        buhlModel = New BuhlmannModel(_Settings)
        Me.MaxDepth = 0

        ' Calculate the tensions from previous dive
        ComputeSurfaceInterval()

        ' Used to calculate the TAT quickly during ascent
        _LastTAT = New TimeSpan(0)

        ' 1 point per FIXEDSTEP seconds
        For t = 0 To totalTimeSec Step FIXEDSTEP
            ' Check if need to cancel the thread
            If BackgroundWorkerCalculatePlan.CancellationPending Then
                Result.Result = DPResults.CANCELED
                BackgroundWorkerCalculatePlan.ReportProgress(101)
                PState = CANCELEDCALCULATION
                e.Cancel = True
                Return
            End If

            If (t = 370) Then
                While (False)

                End While
            End If
            ' Add the point to the list
            AddPointForDetailedOutputAtTime(t, e)

            ' Update the progress
            If (Int(100 * t / totalTimeSec) > percentage) Then
                percentage = Int(100 * t / totalTimeSec)
                BackgroundWorkerCalculatePlan.ReportProgress(percentage)
            End If

            ' Check specials points: end of ascent/descent, end of flat depth, change gas and end of dive.
            For u = t + 1 To t + FIXEDSTEP - 1
                ' TODO : include warnings if necessary
                ' Check if its the end of an ascent/descent
                For i = 0 To _LowDetailedOutputDivePoints.Count - 1
                    If (_LowDetailedOutputDivePoints(i).Time.TotalSeconds = u) Then
                        AddPointForDetailedOutputAtTime(u, e)
                    End If
                Next
            Next
        Next

        ' Terminate the dive in the tissues to record the OTUS and timespans
        buhlModel.TerminateDive(_SurfaceInterval.Add(_DetailedOutputDivePoints(_DetailedOutputDivePoints.Count - 1).Time))

#If PrintDetailsGF = 1 Then
        My.Computer.FileSystem.WriteAllText("details.txt", GFDetailsTextOutput, False)
#End If
    End Sub

    Private Sub AddPointForDetailedOutputAtTime(ByVal tSecond As Double, ByVal e As System.ComponentModel.DoWorkEventArgs)
        Dim d = getDepthAt(New TimeSpan(0, 0, tSecond))
        Dim g = getGasAt(New TimeSpan(0, 0, tSecond))
        Dim sp = getSetPointAt(New TimeSpan(0, 0, tSecond))
        buhlModel.changeGas(g)

        ' Add point to the list, calculates all the details (O2tox, Gas Consumption, etc.)
        Dim p As DivePoint = New DivePoint(_Settings, New TimeSpan(0, 0, tSecond), d, g)
        addDivePointToPlan(buhlModel, _DetailedOutputDivePoints, p, False, True, True, True, g.IsCC, sp, True)

        ' IMPORTANT: TAT calculation makes the calculation time EXPLODE!
        ' It would be wiser to calculate the TAT in a separate thread after all other details were calculated
        ' Calculate the TAT separately
        If (LegsMaxTime.TotalSeconds >= tSecond) Then
            p.TAT = calculateTotalAscentTime(e, _DetailedOutputDivePoints)
            _LastTAT = p.TAT
        Else
            Dim totalSecondsSinceLegsMaxTime As Double = tSecond - LegsMaxTime.TotalSeconds
            p.TAT = _LastTAT.Subtract(New TimeSpan(0, 0, totalSecondsSinceLegsMaxTime))
        End If

        ' Used only for printing details on GF calculation
#If PrintDetailsGF = 1 Then
        GFDetailsTextOutput += "Time " + p.Time.ToString() + "; GF: " + buhlModel.GF().ToString() + Environment.NewLine
            Dim hes = buhlModel.TissuesHe
            Dim n2s = buhlModel.TissuesN2
            For i = 0 To 16
                Dim Pi As Double = n2s(i).Tension + hes(i).Tension
                Dim ai As Double = ((n2s(i).Tension * n2s(i).A) + (hes(i).Tension * hes(i).A)) / Pi
                Dim bi As Double = ((n2s(i).Tension * n2s(i).B) + (hes(i).Tension * hes(i).B)) / Pi
                Dim Mi As Double = ai + ((p.Depth + p.Settings.Pamb) / bi)

                ' Calculate M values
                Dim GFi As Double = (Pi - p.Depth - p.Settings.Pamb) / (Mi - p.Depth - p.Settings.Pamb)

                GFDetailsTextOutput += "Tissue " + i.ToString() + ": GF=" + GFi.ToString()
                GFDetailsTextOutput += "; Mi=" + Mi.ToString()
                GFDetailsTextOutput += "; ai=" + ai.ToString()
                GFDetailsTextOutput += "; bi=" + bi.ToString()
                GFDetailsTextOutput += "; n2tension=" + n2s(i).Tension.ToString()
                GFDetailsTextOutput += "; hetension=" + hes(i).Tension.ToString()
                GFDetailsTextOutput += Environment.NewLine
            Next
            GFDetailsTextOutput += Environment.NewLine
#End If
    End Sub

    ' WARNING: long execution time. MUST be called within a background thread.
    Public Sub CalculatePlanForLostGasScenario(ByVal e As System.ComponentModel.DoWorkEventArgs)
        PState = RESET
        ' CALCULATE THE DIVEPOINTS
        _DivePoints = New List(Of DivePoint)
        _DetailedOutputDivePoints = New List(Of DivePoint)
        buhlModel = New BuhlmannModel(_Settings)
        MaxTimeSpan = New TimeSpan(0, 0, 0)

        If (_LegsList Is Nothing Or _GasList Is Nothing) Then
            Result.Result = DPResults.CANCELED
            BackgroundWorkerCalculatePlan.ReportProgress(0)
            Me.PState = CANCELEDCALCULATION
            Return
        End If

        If (_LegsList.Count = 0 Or _GasList.Count = 0) Then
            Result.Result = DPResults.CANCELED
            BackgroundWorkerCalculatePlan.ReportProgress(0)
            Me.PState = CANCELEDCALCULATION
            Return
        End If

        ' The return of the function
        Result.Result = DPResults.SUCCESS

        ' Build the first points from the given legs.
        BuildPointsFromLegs(e, True)

        ' Calculate the deco
        BuildDecoPoints(e, True)
        If (Me.PState = FAILEDCALCULATION_DECOTOOLONG) Then Return

        ' Record the PO2 max and min, WOBmax and ENDmax
        'GetMinMaxPo2WobEnd()

        ' Generate event to notify the mainApp that the plan is ready, but not the detailedPlan
        'BackgroundWorkerCalculatePlan.ReportProgress(0)

        ' Generate the details for the divewatch
        'GenerateDetailedOutput(e)

        Me.PState = LOSTGASCALCULATED
    End Sub

    Public Sub ReactivateBackgroundWorker()
        If (BackgroundWorkerCalculatePlan Is Nothing) Then
            ' In order to execute the plan calculation in background
            BackgroundWorkerCalculatePlan = New BackgroundWorker()
            BackgroundWorkerCalculatePlan.WorkerReportsProgress = True
            BackgroundWorkerCalculatePlan.WorkerSupportsCancellation = True
            AddHandler BackgroundWorkerCalculatePlan.ProgressChanged, AddressOf BackgroundWorkerCalculatePlan_ReportProgress
            AddHandler BackgroundWorkerCalculatePlan.DoWork, AddressOf BackgroundWorkerCalculatePlan_DoWork
            AddHandler BackgroundWorkerCalculatePlan.RunWorkerCompleted, AddressOf BackgroundWorkerCalculatePlan_Completed
        End If
    End Sub

    Private Sub BackgroundWorkerCalculatePlan_DoWork(ByVal sender As System.Object,
                 ByVal e As System.ComponentModel.DoWorkEventArgs)

        ' CALCULATE THE DIVEPOINTS
        Me.Result.CalculatingDivePlanDetails = True
        _DivePoints = New List(Of DivePoint)
        _DetailedOutputDivePoints = New List(Of DivePoint)
        buhlModel = New BuhlmannModel(_Settings)
        MaxTimeSpan = New TimeSpan(0, 0, 0)

        If (_LegsList Is Nothing Or _GasList Is Nothing) Then
            Result.Result = DPResults.CANCELED
            BackgroundWorkerCalculatePlan.ReportProgress(0)
            PState = CANCELEDCALCULATION
            Return
        End If

        If (_LegsList.Count = 0 Or _GasList.Count = 0) Then
            Result.Result = DPResults.CANCELED
            BackgroundWorkerCalculatePlan.ReportProgress(0)
            PState = CANCELEDCALCULATION
            Return
        End If

        FirstDecoStopDepth = 0

        ' The return of the function
        Result.Result = DPResults.SUCCESS

        ' Compute the Surface Interval tensions
        ComputeSurfaceInterval()

        ' Build the first points from the given legs.
        BuildPointsFromLegs(e, False)

        ' Calculate the deco
        BuildDecoPoints(e, False)
        If (_State = FAILEDCALCULATION_DECOTOOLONG) Then
            Return
        End If

        ' Record the PO2 max and min, WOBmax and ENDmax
        GetMinMaxPo2WobEnd()

        ' Copy the results to a safe output
        _LowDetailedOutputDivePoints = New List(Of DivePoint)
        For Each p In _DivePoints
            _LowDetailedOutputDivePoints.Add(p)
        Next
        _OutputMaxTimeSpan = MaxTimeSpan

        ' Generate event to notify the mainApp that the plan is ready, but not the detailedPlan
        BackgroundWorkerCalculatePlan.ReportProgress(0)

        ' Generate the details for the divewatch
        GenerateDetailedOutput(e)

        ' Generate summary plan
        SummaryPlan = New Summary(Me)

        ' Generate event to notify the mainApp that the plan is ready, but not the detailedPlan
        BackgroundWorkerCalculatePlan.ReportProgress(101)
    End Sub

    Private Sub BackgroundWorkerCalculatePlan_ReportProgress(
    ByVal sender As Object,
    ByVal e As ProgressChangedEventArgs)
        If (e.ProgressPercentage = 0) Then
            ' Trigger event
            OnPlanReady()
            PState = CALCULATINGDETAILS
        ElseIf (e.ProgressPercentage = 101) Then
            OnPlanDetailsReady()
            PState = DETAILSCALCULATED
        Else
            CalculationState = e.ProgressPercentage
            OnPlanDetailsCalculationStateUpdate()
            PState = CALCULATINGDETAILS
        End If
    End Sub

    Private Sub BackgroundWorkerCalculatePlan_Completed(
    ByVal sender As Object,
    ByVal e As RunWorkerCompletedEventArgs)
        If CalculatePlanOperationWaiting Then
            CalculatePlanOperationWaiting = False
            BackgroundWorkerCalculatePlan.RunWorkerAsync()
        Else
            Me.Result.CalculatingDivePlanDetails = False
        End If
    End Sub

    Private Function selectBestDecoGas(ByVal currentDepth As Double, ByVal currentGas As Gas) As Gas
        If (currentGas.IsCC) Then
            Return selectBestCCdecoGas(currentDepth, currentGas)
        Else
            Return selectBestOCDecoGas(currentDepth, currentGas)
        End If
    End Function

    Private Function selectBestCCdecoGas(ByVal currentDepth As Double, ByVal currentGas As Gas) As Gas
        ' Find the gas with highest PPO2 which doesn't exceed PPO2max
        Dim maxPPO2 = currentGas.PPO2(currentDepth)

        ' Look among all available gases
        For Each gas In _GasList
            ' Arbitraty value of 1.05 atm for the max value
            If (gas.Available And gas.IsCC And gas.PPO2(currentDepth) > maxPPO2 And gas.PPO2(currentDepth) <= 1.05) Then
                currentGas = gas
                maxPPO2 = gas.PPO2(currentDepth)
            End If
        Next

        Return currentGas
    End Function

    Private Function selectBestOCDecoGas(ByVal currentDepth As Double, ByVal currentGas As Gas) As Gas
        ' Find the gas with highest PPO2 which doesn't exceed PPO2max
        Dim maxPPO2 = currentGas.PPO2(currentDepth)
        ' Current gas cannot be used if above MaxPPO2
        If (maxPPO2 > currentGas.PPO2Max) Then
            maxPPO2 = 0
        End If

        ' Look among all available gases
        For Each gas In _GasList
            If (gas.Available And gas.IsOC And gas.PPO2(currentDepth) > maxPPO2 And gas.PPO2(currentDepth) <= gas.PPO2Max) Then
                currentGas = gas
                maxPPO2 = gas.PPO2(currentDepth)
            End If
        Next

        Return currentGas
    End Function

    ' Record in the gas list that the specified gas was used
    Private Sub useGas(ByVal gas As Gas)
        For Each g In _GasList
            If (g.ID = gas.ID) Then
                g.UsedGas = True
                Return
            End If
        Next
    End Sub

    ' Adds a given point to the plan, and then calculates the tensions and MaxTimeSpan
    Private Sub addDivePointToPlan(ByVal tis As BuhlmannModel, ByVal dpList As List(Of DivePoint),
                                   ByVal p As DivePoint, ByVal modelisingLeg As Boolean, ByVal O2tox As Boolean,
                                   ByVal gasConsum As Boolean, ByVal Details As Boolean,
                                   ByVal isClosedCircuit As Boolean, ByVal setpoint As Double,
                                   ByVal calculateNoDecoTime As Boolean)
        Dim donotchangeSP As Boolean = False

        ' Do not add the point if identical to previous point
        Dim lastP As DivePoint
        If (dpList.Count = 0) Then
            Dim gas = _GasList(0)
            If (Me.Legs.Count > 0) Then gas = Me.Legs(0).Gas

            ' Create a new divepoint if its the first one of the diveplan
            lastP = New DivePoint(_Settings, New TimeSpan(0), 0, gas)
            lastP.SetPoint = setpoint
            lastP.GasCapacity = gas.Capacity
            If (_PreviousDivePlan IsNot Nothing) Then
                lastP.OTUD = buhlModel.OTUD
                lastP.OTUS = buhlModel.OTUS
                lastP.GF = buhlModel.calculateActualCurrentGF()
            End If
            lastP.NoDecoTime = tis.getNoDecoLimit()
            dpList.Add(lastP)
        Else
            lastP = dpList(dpList.Count - 1)
        End If

        If p.Depth = lastP.Depth And p.Time = lastP.Time And
           p.Gas.ID = lastP.Gas.ID And p.SetPoint = lastP.SetPoint Then
            Return
        End If


        ' Check if need to activate the autoswitch. If so, add an intermediate point
        If (isClosedCircuit And p.Gas.IsCC) Then
            ' Descent (AutoSwitchHigh)
            If (_Settings.AutoSwitchToHighEnabled And p.Depth > lastP.Depth _
               And (Not AutoSwitchHighTriggered) _
               And lastP.Depth < _Settings.AutoSwitchToHigh And p.Depth >= _Settings.AutoSwitchToHigh) Then
                AutoSwitchHighTriggered = True
                If (p.Depth = _Settings.AutoSwitchToHigh) Then
                    p.SetPoint = _Settings.HighSetPoint
                    donotchangeSP = True
                    p.AutoswitchToHigh = True
                Else
                    ' Create a new dive point
                    Dim descRate As Double = (p.Depth - lastP.Depth) / (p.Time.TotalSeconds - lastP.Time.TotalSeconds)
                    Dim p2timeSec As Double = lastP.Time.TotalSeconds + ((_Settings.AutoSwitchToHigh - lastP.Depth) / descRate)
                    Dim p2 = New DivePoint(_Settings, New TimeSpan(0, 0, p2timeSec), _Settings.AutoSwitchToHigh, p.Gas)
                    p2.SetPoint = _Settings.HighSetPoint
                    setpoint = p2.SetPoint
                    p2.AutoswitchToHigh = True

                    ' Recursive function call
                    addDivePointToPlan(tis, dpList, p2, modelisingLeg, O2tox, gasConsum, Details, isClosedCircuit, p2.SetPoint, calculateNoDecoTime)
                End If

            ElseIf (_Settings.AutoSwitchToLowEnabled And p.Depth < lastP.Depth _
               And (Not AutoSwitchLowTriggered) _
               And lastP.Depth > _Settings.AutoSwitchToLow And p.Depth <= _Settings.AutoSwitchToLow) Then
                AutoSwitchLowTriggered = True
                If (p.Depth = _Settings.AutoSwitchToLow) Then
                    p.SetPoint = _Settings.LowSetPoint
                    donotchangeSP = True
                    p.AutoswitchToLow = True
                Else
                    ' Create a new dive point
                    Dim ascRate As Double = (lastP.Depth - p.Depth) / (p.Time.TotalSeconds - lastP.Time.TotalSeconds)
                    Dim p2timeSec As Double = lastP.Time.TotalSeconds + ((lastP.Depth - _Settings.AutoSwitchToLow) / ascRate)
                    Dim p2 = New DivePoint(_Settings, New TimeSpan(0, 0, p2timeSec), _Settings.AutoSwitchToLow, p.Gas)
                    p2.SetPoint = _Settings.LowSetPoint
                    setpoint = p2.SetPoint
                    p2.AutoswitchToLow = True

                    ' Recursive function call
                    addDivePointToPlan(tis, dpList, p2, modelisingLeg, O2tox, gasConsum, Details, isClosedCircuit, p2.SetPoint, calculateNoDecoTime)
                End If
            End If

        End If

        dpList.Add(p)
        useGas(p.Gas)
        tis.changeGas(p.Gas)

        ' CALCULATE TENSIONS & OXYGEN TOXICITY
        Dim AscDesRate
        Dim lastP2 = lastP
        If (dpList.Count > 1) Then lastP2 = dpList(dpList.Count - 2)
        Dim Duration = New TimeSpan(0, 0, p.Time.TotalSeconds - lastP2.Time.TotalSeconds)
        If (lastP.Depth = p.Depth Or p.Time.TotalSeconds = lastP.Time.TotalSeconds) Then
            AscDesRate = 0
        Else
            AscDesRate = (p.Depth - lastP.Depth) / (p.Time.TotalSeconds - lastP.Time.TotalSeconds) * 60
        End If

        ' From previousPoint to p, the setpoint used is previousPoint's setpoint, not p's
        Dim setPoint2 = p.SetPoint
        If (dpList.Count > 1) Then setPoint2 = dpList(dpList.Count - 2).SetPoint
        tis.goToDepth(p.Depth, AscDesRate, Duration, O2tox, setPoint2)


        If (O2tox) Then
            p.OTUD = tis.OTUD
            p.OTUS = tis.OTUS
        End If

        ' Record SetPoint
        If (isClosedCircuit And Not donotchangeSP) Then
            p.SetPoint = setpoint
        End If

        ' CALCULATE GAS CONSUMPTION
        If (gasConsum And (Not isClosedCircuit)) Then
            Dim rmv = _Settings.RMVDive
            If (TimeSpan.Compare(p.Time, LegsMaxTime) > 0) Then
                rmv = _Settings.RMVDeco
            End If
            If (modelisingLeg) Then
                rmv = _Settings.RMVDive
            End If

            getGasByID(p.Gas).consumeGas(lastP, p, rmv)

            Dim gas As Gas = getGasByID(p.Gas)
            p.GasCapacity = getGasByID(p.Gas).Capacity
            p.GasConsummed = gas.Volume - p.GasCapacity
            p.GTR = p.Gas.GTR(p.Depth, _Settings.RMVDive)

            ' Check if the reserve was reached
            If (p.Gas.ReserveWarningRaised = False) Then
                If (p.Gas.IsOnReserve) Then
                    p.Gas.ReserveWarningRaised = True
                    p.Gas.TimeOfReserveWarning = p.Time
                End If
            End If
            If (p.Gas.OutOfGasWarningRaised = False) Then
                If (p.Gas.Capacity <= 0) Then
                    p.Gas.OutOfGasWarningRaised = True
                    p.Gas.TimeOfOutOfGasWarning = p.Time
                End If
            End If
        End If

        ' Calculate the additional information
        If (Details) Then
            'p.NDL = tis.getNoDecoLimit()   ' NDL is implemented but not used
            p.GF = tis.GF

            If (Not isClosedCircuit) Then
                p.ENDa = p.Gas.ENDa(p.Depth, _Settings)
                p.EAD = p.Gas.EAD(p.Depth)
            End If

            ' Record the deepest stop
            If (TimeSpan.Compare(p.Time, LegsMaxTime) > 0 And p.Depth = tis.DeepestStop) Then
                ' If after the last leg, the current depth is equal to the deepest stop of the tissue,
                ' it means that the current depth is the First Deco Stop. So lock the deepest stop
                ' such that it is not recalculated
                tis.LockDeepestStop()
            End If

            If (isClosedCircuit) Then
                If (TimeSpan.Compare(p.Time, LegsMaxTime) >= 0) Then tis.DeepestStop = tis.getCeiling(setpoint)

                If (TimeSpan.Compare(p.Time, LegsMaxTime) > 0) Then
                    p.Ceiling = tis.getCeiling(setpoint)
                Else
                    p.Ceiling = tis.getCeiling(setpoint)
                End If
            Else
                If (TimeSpan.Compare(p.Time, LegsMaxTime) >= 0) Then tis.DeepestStop = tis.getCeiling()

                If (TimeSpan.Compare(p.Time, LegsMaxTime) > 0) Then
                    p.Ceiling = tis.getCeiling()
                Else
                    p.Ceiling = tis.getCeiling()
                End If
            End If

            If (MaxDepth < p.Depth) Then
                MaxDepth = p.Depth
            End If
            p.MaxDepth = MaxDepth
        End If

        ' Calcualte the NoDecoTime
        If (calculateNoDecoTime) Then
            p.NoDecoTime = tis.getNoDecoLimit()
        End If
    End Sub

    ' Return true if all outputs are identical (no need to redraw the graph, which incures blinking)
    Public Function isOutputIdentical(ByVal divePlan As DivePlan) As Boolean
        If divePlan.DivePoints.Count <> Me.DivePoints.Count Then
            Return False
        End If

        For i As Integer = 0 To Me.DivePoints.Count - 1

        Next


        Return True
    End Function

    ' Returns the depth at the given TimeSpan
    Public Function getDepthAt(ByVal time As TimeSpan) As Double
        For i = 0 To _DivePoints.Count - 1
            Dim p As DivePoint = _DivePoints(i)
            If TimeSpan.Compare(time, p.Time) <= 0 Then
                If time.TotalSeconds = p.Time.TotalSeconds Then
                    Return p.Depth
                End If

                Dim q As DivePoint = _DivePoints(i - 1)
                Return p.Depth + ((q.Depth - p.Depth) / (q.Time.TotalSeconds - p.Time.TotalSeconds) * (time.TotalSeconds - p.Time.TotalSeconds))
            End If
        Next

        Return 0
    End Function

    ' Returns the used Gas at the given TimeSpan
    Private Function getGasAt(ByVal time As TimeSpan)
        For i = 0 To _DivePoints.Count - 1
            Dim p As DivePoint = _DivePoints(i)
            If TimeSpan.Compare(time, p.Time) <= 0 Then
                If time.TotalSeconds = p.Time.TotalSeconds Then
                    Return getGasByID(p.Gas)
                End If

                Dim q As DivePoint = _DivePoints(i - 1)
                Return getGasByID(p.Gas) ' TODO: here, bug ? Should be q? To check
            End If
        Next

        Return 0
    End Function

    Private Function getSetPointAt(ByVal time As TimeSpan)
        For i = 0 To _DivePoints.Count - 1
            Dim p As DivePoint = _DivePoints(i)
            If TimeSpan.Compare(time, p.Time) <= 0 Then
                If time.TotalSeconds = p.Time.TotalSeconds Then
                    Return p.SetPoint
                End If

                Dim q As DivePoint = _DivePoints(i - 1)
                Return q.SetPoint
            End If
        Next

        Return 0
    End Function

    ' Returns -1 if the divepoint is not ready yet (diveplan still building in background thread)
    ' or time out of range
    ' Returns the requested detailed dive point otherwise
    Public Function getDivePointAt(ByVal time As TimeSpan)
        If (_DetailedOutputDivePoints Is Nothing) Then
            Return -1
        End If

        If (time.TotalSeconds > _DetailedOutputDivePoints(_DetailedOutputDivePoints.Count - 1).Time.TotalSeconds) Then
            Return -1
        End If

        Dim i = System.Math.Floor(time.TotalSeconds / FIXEDSTEP)
        While i <= _DetailedOutputDivePoints.Count - 1
            If (_DetailedOutputDivePoints(i).Time.TotalSeconds > time.TotalSeconds) Then
                ' Return the closest point (left, if in the exact middle)
                If (_DetailedOutputDivePoints(i).Time.TotalSeconds - time.TotalSeconds >=
                    time.TotalSeconds - _DetailedOutputDivePoints(i - 1).Time.TotalSeconds) Then
                    Return _DetailedOutputDivePoints(i - 1)
                Else
                    Return _DetailedOutputDivePoints(i)
                End If
            End If

            i = i + 1
        End While

        Return _DetailedOutputDivePoints.Last
    End Function


    Public Function getMaxPO2onBranch(ByVal startTime As TimeSpan, ByVal stopTime As TimeSpan) As Double
        For i = 0 To _DivePoints.Count - 2
            If (TimeSpan.Compare(startTime, _DivePoints(i).Time) = 0) Then
                ' Corrected bug. Must use the gas of I+1 
                Dim maxPO2 = _DivePoints(i + 1).Gas.PO2(_DivePoints(i).Depth)

                If (_DivePoints(i + 1).Gas.PO2(_DivePoints(i + 1).Depth) > maxPO2) Then
                    Return _DivePoints(i + 1).Gas.PO2(_DivePoints(i + 1).Depth)
                End If

                Return maxPO2
            End If
        Next

        Return 0.0
    End Function

    Public Function getMinPO2onBranch(ByVal startTime As TimeSpan, ByVal stopTime As TimeSpan) As Double
        For i = 0 To _DivePoints.Count - 2
            If (TimeSpan.Compare(startTime, _DivePoints(i).Time) = 0) Then
                Dim minPO2 = _DivePoints(i).Gas.PO2(_DivePoints(i).Depth)

                If (_DivePoints(i + 1).Gas.PO2(_DivePoints(i + 1).Depth) < minPO2) Then
                    Return _DivePoints(i + 1).Gas.PO2(_DivePoints(i + 1).Depth)
                End If

                Return minPO2
            End If
        Next

        Return 0.0
    End Function

    Public Function getMaxENDonBranch(ByVal startTime As TimeSpan, ByVal stopTime As TimeSpan) As Double
        For i = 0 To _DivePoints.Count - 2
            If (TimeSpan.Compare(startTime, _DivePoints(i).Time) = 0) Then
                Dim maxEND = _DivePoints(i).Gas.ENDa(_DivePoints(i).Depth, _Settings)

                If (_DivePoints(i + 1).Gas.ENDa(_DivePoints(i + 1).Depth, _Settings) > maxEND) Then
                    Return _DivePoints(i + 1).Gas.PO2(_DivePoints(i + 1).Depth)
                End If

                Return maxEND
            End If
        Next

        Return 0.0
    End Function

    Public Function getMaxWOBonBranch(ByVal startTime As TimeSpan, ByVal stopTime As TimeSpan) As Double
        For i = 0 To _DivePoints.Count - 2
            If (TimeSpan.Compare(startTime, _DivePoints(i).Time) = 0) Then
                Dim maxWOB = _DivePoints(i).Gas.WOB(_DivePoints(i).Depth)

                If (_DivePoints(i + 1).Gas.WOB(_DivePoints(i + 1).Depth) > maxWOB) Then
                    Return _DivePoints(i + 1).Gas.PO2(_DivePoints(i + 1).Depth)
                End If

                Return maxWOB
            End If
        Next

        Return 0.0
    End Function

    Public Function getGasConsumptionOnBranch(ByVal startTime As TimeSpan, ByVal stopTime As TimeSpan) As Double
        Dim p1 As DivePoint = New DivePoint(_Settings, startTime, getDepthAt(startTime), getGasAt(startTime))
        Dim p2 As DivePoint = New DivePoint(_Settings, stopTime, getDepthAt(stopTime), getGasAt(stopTime))

        ' Create new Gas
        Dim gg As Gas = getGasAt(startTime)
        Dim g As Gas = New Gas("0", gg.ID, gg.FO2, gg.FHe, gg.PPO2Max, gg.Available, gg.Color)

        Dim rmv = _Settings.RMVDive
        If (TimeSpan.Compare(p2.Time, LegsMaxTime) > 0) Then
            rmv = _Settings.RMVDeco
        End If
        g.consumeGas(p1, p2, rmv)

        Return g.TotalUsedGas
    End Function

    ' Calculates the total ascent time for the given depth and tissues (tensions)
    ' Highly recommended to execute this call in a background thread as it requires a lot of calculation
    Private Function calculateTotalAscentTime(ByVal e As System.ComponentModel.DoWorkEventArgs,
                                              ByVal divePoints As List(Of DivePoint)) As TimeSpan
        ' Copy the current diveplan
        Dim dp As DivePlan = Me.CloneCurrentState()

        ' Copy the dive points to a new list
        Dim tmpDivePoints As List(Of DivePoint) = New List(Of DivePoint)
        tmpDivePoints.Add(divePoints(divePoints.Count - 1))

        ' Get the time of the last point
        Dim tmSpn1 = tmpDivePoints(tmpDivePoints.Count - 1).Time

        ' Build the deco from this point
        dp.DivePoints = tmpDivePoints

        ' Calculate on the temporary diveplan
        dp.BuildDecoPoints(e, False)

        ' Read the last time
        Dim tmSpn2 = dp.DivePoints.Last.Time

        Return (tmSpn2 - tmSpn1)
    End Function

    ''' <summary>
    ''' In CC, the SP can be changed during the ascent, changing the ceiling. Therefore,
    ''' the ceiling cannot be calculated directly inside the BuhlmannModel class.
    ''' This function is to be used during the Deco Calculations only.
    ''' 
    ''' Uses current buhlModel, and current depth in buhlModel.
    ''' </summary>
    ''' <returns>Ceiling in feet including gas switches and SP switches.</returns>
    Public Function ceiling() As Double
        ' Make a tmp copy of the current diveplan
        Dim dp As DivePlan = Me.CloneCurrentState()
        Dim initialDepth As Double = dp.DivePoints.Last.Depth
        Dim decoSteps As Double = _Settings.decoStep
        Dim targetDepth As Double = Math.RoundToNearest(initialDepth / 2, decoSteps)
        Dim stepp As Double = targetDepth / 2

        ' Check that the gf is correct before ascending
        If (Not dp.isGFrespected(False)) Then Return initialDepth

        ' Search, using middle technic
        While (True)
            Dim res = isCeiling(targetDepth)
            If (res = 0) Then
                Return targetDepth
            End If

            If (res > 0) Then
                targetDepth = Math.RoundToNearest(targetDepth + stepp, decoSteps)
            Else
                targetDepth = Math.RoundToNearest(targetDepth - stepp, decoSteps)
            End If
            stepp /= 2
            If (stepp < decoSteps) Then stepp = decoSteps
        End While
        Return initialDepth
    End Function

    ' Function used to search efficiently the ceiling
    Private Function isCeiling(ByVal targetDepth As Double) As Integer
        Dim dp As DivePlan = Me.CloneCurrentState()
        dp.DecoAscendTo(targetDepth, True, False, False)

        If (dp.isGFrespected(False)) Then
            ' If targetDepth is 0, then ceiling is ok
            If (targetDepth <= 0) Then Return 0

            ' Check GF just below. Should be False
            dp = Me.CloneCurrentState()
            dp.DecoAscendTo(targetDepth - _Settings.decoStep, True, False, False)
            If (Not dp.isGFrespected(False)) Then
                Return 0    ' Ceiling was found
            Else
                Return -1   ' Ceiling is lower
            End If
        End If
        Return 1            ' Ceiling is higher
    End Function

    Private Function isNoDecoDive() As Boolean
        ' Make a tmp copy of the current diveplan
        Dim dp As DivePlan = Me.CloneCurrentState()

        dp.DecoAscendTo(0, True, True, False)        ' Go to 0 ft depth
        ' After the ascent to 0, no need to change the gas

        Return dp.isGFrespected(True)
    End Function

    Private Function CloneCurrentState() As DivePlan
        Dim dp As DivePlan = New DivePlan(_PreviousDivePlan, _Settings, GasListArray, LegsList)

        ' Copy the buhlModel state
        dp.buhlModel = Me.buhlModel.Clone()

        ' Copy the last divepoint (sufficient)
        dp.DivePoints.Add(Me.DivePoints.Last())

        ' Copy the triggers
        dp.AutoSwitchHighTriggered = Me.AutoSwitchHighTriggered
        dp.AutoSwitchLowTriggered = Me.AutoSwitchLowTriggered

        ' Important for the calculations
        dp.MaxTimeSpan = Me.MaxTimeSpan

        ' Also required for decoTime calculatations (isGFrespected)
        dp.FirstDecoStopDepth = Me.FirstDecoStopDepth

        Return dp
    End Function

    ' Goes to given depth (ascent), switching SP and gases if needed
    ' Assumes there is no deco stop required between the current depth 
    ' and the target depth
    ' Allows to abort the function execution at anytime if the max authorized 
    ' GF is passed.
    Private Sub DecoAscendTo(ByVal targetDepth As Double, ByVal abortIfGFcrossed As Boolean,
                             ByVal CheckingNoDecoDive As Boolean, ByVal gasConsum As Boolean)
        Dim curDepth As Double = DivePoints.Last.Depth
        Dim curGas As Gas = DivePoints.Last.Gas
        Dim setpoint As Double = DivePoints.Last.SetPoint

        If (abortIfGFcrossed) Then
            If (Not isGFrespected(CheckingNoDecoDive)) Then Return
        End If

        ' If first ascent, multiple gas switches are allowed during the ascent
        If (FirstDecoStopDepth > 0 And curDepth > FirstDecoStopDepth) Then
            While (curDepth > targetDepth And curGas.ID <> selectBestDecoGas(targetDepth, curGas).ID)
                ' Find the depth of gas change
                While (curGas.ID = selectBestDecoGas(curDepth, curGas).ID)
                    If (_Settings.Units = Settings.IMPERIAL) Then
                        curDepth = curDepth - 1
                    Else
                        curDepth = curDepth - MtoFT
                    End If
                End While

                ' Double bring approximation by the order of E-14
                If (System.Math.Abs(curDepth) < 1.0) Then curDepth = 0
                If (System.Math.Abs(targetDepth - curDepth) < 1.0) Then curDepth = targetDepth

                ' Go to the found depth
                Dim ascTime1 = (DivePoints.Last.Depth - curDepth) * 60 / BuhlmannModel.Ar(_Settings)
                Dim p1 As DivePoint = New DivePoint(_Settings, MaxTimeSpan.Add(New TimeSpan(0, 0, ascTime1)), curDepth, curGas)
                addDivePointToPlan(buhlModel, DivePoints, p1, False, False, gasConsum, False, curGas.IsCC, setpoint, False)
                setpoint = DivePoints.Last.SetPoint

                ' Update MaxTimeSpan
                MaxTimeSpan = DivePoints.Last.Time

                ' Abort the function if GF is passed
                If (abortIfGFcrossed) Then
                    If (Not isGFrespected(CheckingNoDecoDive)) Then Return
                End If

                ' Change gas
                If (ChangeGasIfAnyBetter(gasConsum)) Then
                    curGas = buhlModel.CurrentGas
                End If
                'curGas = selectBestOCDecoGas(curDepth, curGas)
                'buhlModel.changeGas(curGas)
                'useGas(curGas)
            End While

            ' Final point at targetDepth
            Dim ascTime2 = (DivePoints.Last.Depth - targetDepth) * 60 / BuhlmannModel.Ar(_Settings)
            Dim p2 As DivePoint = New DivePoint(_Settings, MaxTimeSpan.Add(New TimeSpan(0, 0, ascTime2)), targetDepth, curGas)
            addDivePointToPlan(buhlModel, DivePoints, p2, False, False, gasConsum, False, curGas.IsCC, setpoint, False)
        Else
            ' Not the first ascent: change gas only when reaching the target depth

            ' Final point at targetDepth
            Dim ascTime2 = (DivePoints.Last.Depth - targetDepth) * 60 / BuhlmannModel.Ar(_Settings)
            Dim p2 As DivePoint = New DivePoint(_Settings, MaxTimeSpan.Add(New TimeSpan(0, 0, ascTime2)), targetDepth, curGas)
            addDivePointToPlan(buhlModel, DivePoints, p2, False, False, gasConsum, False, curGas.IsCC, setpoint, False)
        End If

        MaxTimeSpan = DivePoints.Last.Time
    End Sub

    Private Sub StayForDeco(ByVal gas As Gas, ByVal duration As TimeSpan, ByVal gasConsum As Boolean,
                            ByVal O2tox As Boolean)
        Dim curDepth As Double = DivePoints.Last.Depth
        Dim setpoint As Double = DivePoints.Last.SetPoint

        Dim p As DivePoint = New DivePoint(_Settings, MaxTimeSpan.Add(duration), curDepth, gas)
        addDivePointToPlan(buhlModel, DivePoints, p, False, O2tox, gasConsum, False, gas.IsCC, setpoint, False)

        MaxTimeSpan = DivePoints.Last.Time
    End Sub

    ' Notice this function does not check the GF conditions
    Private Sub LegGoTo(ByVal targetDepth As Double, ByVal AscentRate As Double,
                        ByVal DescentRate As Double, ByVal gasConsum As Boolean,
                        ByVal setpoint As Double)
        Dim curDepth As Double = DivePoints.Last.Depth
        Dim curGas As Gas = buhlModel.CurrentGas

        ' If setpoint is different, then add a point
        If (_DivePoints.Last.SetPoint <> setpoint) Then
            Dim p1 As DivePoint = New DivePoint(_Settings, _DivePoints.Last.Time, _DivePoints.Last.Depth, curGas)
            p1.SetPoint = setpoint
            addDivePointToPlan(buhlModel, DivePoints, p1, False, False, gasConsum, False, curGas.IsCC, setpoint, False)
        End If

        ' Final point at targetDepth
        Dim ascTime2 As Double
        If (DivePoints.Last.Depth - targetDepth < 0) Then
            ascTime2 = System.Math.Abs(DivePoints.Last.Depth - targetDepth) * 60 / DescentRate
        Else
            ascTime2 = System.Math.Abs(DivePoints.Last.Depth - targetDepth) * 60 / AscentRate
        End If
        Dim p2 As DivePoint = New DivePoint(_Settings, MaxTimeSpan.Add(New TimeSpan(0, 0, ascTime2)), targetDepth, curGas)
        p2.SetPoint = setpoint
        addDivePointToPlan(buhlModel, DivePoints, p2, False, False, gasConsum, False, curGas.IsCC, setpoint, False)

        MaxTimeSpan = DivePoints.Last.Time
        If Me.MaxDepth < _DivePoints.Last.Depth Then
            Me.MaxDepth = _DivePoints.Last.Depth
        End If
    End Sub

    Private Function isGFrespected(ByVal CheckingNoDecoDive As Boolean) As Boolean
        ' If CheckingNoDecoDive is True, then GFhigh is applied
        ' Other checking GFlow if FirstDecoStopDepth is unknown
        If (CheckingNoDecoDive) Then FirstDecoStopDepth = -1
        If (buhlModel.GF > _Settings.getGFmaxAtDepth(DivePoints.Last.Depth, FirstDecoStopDepth) * 100.0) Then Return False
        Return True
    End Function

    ' For any given depth, returns the next available deco stop depth (in feet)
    Private Function closestDecoStop(ByVal depth As Double) As Double
        If (_Settings.Units = Settings.METRIC) Then
            Dim deco As Integer = Int(depth / MtoFT) ' value in meters
            deco = deco - (deco Mod Int(_Settings.decoStep() / MtoFT))
            Return deco * MtoFT
        Else
            Return (depth - (depth Mod Int(_Settings.decoStep())))
        End If
    End Function

    ' Returns True if the gas was changed
    Private Function ChangeGasIfAnyBetter(ByVal gasconsum As Boolean) As Boolean
        Dim depth As Double = DivePoints.Last.Depth
        Dim gas As Gas = DivePoints.Last.Gas

        ' If using Extended Mix Switch Stop, then stop
        If (_Settings.ExtendedMixSwitchStop.TotalMinutes > 0) Then
            StayForDeco(gas, _Settings.ExtendedMixSwitchStop, gasconsum, False)
        End If

        ' If any better
        If (gas.ID <> selectBestDecoGas(depth, gas).ID) Then
            ' Change gas
            gas = selectBestDecoGas(depth, gas)
            buhlModel.changeGas(gas)
            useGas(gas)

            Return True
        End If



        Return False
    End Function

    ' Delegate for the PlanReady event - Raised when the thread has calculated the plan,
    ' but not generated yet the detailed output
    <NonSerialized>
    Public Event PlanReady As EventHandler
    <NonSerialized>
    Public Event PlanDetailsReady As EventHandler
    <NonSerialized>
    Public Event PlanDetailsCalculationStateUpdate As EventHandler
    <NonSerialized>
    Public Event PlanStateChanged As EventHandler

    ' Raise the PlanReady event
    Protected Sub OnPlanReady()
        RaiseEvent PlanReady(Me, EventArgs.Empty)
    End Sub
    Protected Sub OnPlanDetailsReady()
        RaiseEvent PlanDetailsReady(Me, EventArgs.Empty)
    End Sub
    Protected Sub OnPlanDetailsCalculationStateUpdate()
        RaiseEvent PlanDetailsCalculationStateUpdate(Me, EventArgs.Empty)
    End Sub
    Protected Sub OnPlanStateChanged()
        RaiseEvent PlanStateChanged(Me, EventArgs.Empty)
    End Sub
End Class


<Serializable()>
Public Class DPResults
    Public Const SUCCESS As Integer = 1
    Public Const FAILED As Integer = 0
    Public Const CANCELED As Integer = 2

    Public Result As Integer = FAILED
    Public Message As String = ""
    Public CalculatingDivePlanDetails As Boolean = False
End Class