


@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun PropertyDetailScreen(
    propertyId:String,
    onNavigateBack:()->Unit,
    onNavigateToBooking:(String)->Unit,
    onNavigateToChat:(String)->Unit,
    viewMode:PropertyViewModel=hiltViewModel()
)
{

    val uiState by viewModel.uiState=collectAsState()

    LaunchedEffect(propertyId){
        viewModel.loadProperty(propertyId)
    }
    Scaffold(
        containerColor=ScreenBackground,
        topBar={
            MiCasaTopBar(
                title={},
                navigationIcon={
                    IconButton(onClick=onNavigateBack,
                    modifer=Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                    ){
                        Icon(
                           imageVector=Icons.Rounded.ArrowBackIosNew,
                           contentDescription="Back",
                           tint=HeadingText,
                           modifier=Modifier.size(18.dp)
                       )
                   }
               },
               colors=TopAppBarDefaults.topAppBarColors(
                   containerColor=Color.Transparent
               )
           )
       },
       bottomBar={
        when(val state=uiState)
        {
            if PropertyUiState.DetailSuccess->{
                PropertyBottomBar(
                    property=state.property,
                    onBook={onNavigateToBooking(propertyId)}
                    onChat={onNavigateToChat(state.property.ownerId)}
                )
            }
            else -> {}
        }
    }
){ paddingValues->
    when (val state=uiState)
    {
        is PropertyUiState.Loading->{
            PropertyDetailSkeleton(paddingValues)
        }
        is PropertyUiState.DetailSuccess ->{
            PropertyDetailContent(
                property=state.property,
                paddingValues=paddingValues
            )
        }
        is PropertyUiState.Error -> {
            Box(
                modifier=Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment=Alligment.Center
            )
            {Text(state.message,color=ErrorColor
            }
        }
        else-> {}
    }
  }
} 
