package com.mobile.micasaestucasa.ui.screens.chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId:String,
    hostId:String,
    renterId:String,
    propertyId:String,
    currentUserId:String,
    onNavigateBack:()->Unit,
    viewModel:ChatViewModel=hiltViewModel()
)
{
    val messages by viewModel.collectAsState()
    val listState= rememberLazyListState()
    val scope = rememberCorutineScope()
    var inputText by remember {mutableStateOf("")}
    var selectedImageUri by remember {mutableStateOf<Uri?>(null)}

    //picker of img
    val imagePicker=rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ){uri -> selectedImageUri = uri}
    LaunchedEffect(conversationId)
    {
        viewModel.openConversation(hostId,renterId,propertyId)
    }
    LaunchedEffect(message.size)
    {
        if(message.isNotEmpty())
        {
            scope.launch{
                listState.animateScrollToItem(message.size-1)
            }
        }
    }
    //marks as read when opens screen
    LaunchedEffect(conversationId)
    {
        viewModel.markAsRead(conversationId,currentUserId)
    }
    Scaffold(
        containerColor=ScreenBackground,
        topBar={
            TopAppBar(
                title={
                    Row(verticalAlignment=Alignment.CenterVertically)
                    {
                        Box(
                        modifier=Modifier.size(36.dp).clip(CircleShape).background(Sfumatura),contentAlignment=Alignment.Center){
                            Icon(Icons.Rounded.Person,null,tint=Primario,modifier=Modifier.size(20.dp))
                        }
                        Spacer(modifier=Modifier.width(10.dp))
                        Column{
                            Text(
                                text = if(currentUserId==hostId) "Renter" else "Owner",
                                fontWeight=FontWeight.SemiBold,
                                fontSize=15.sp,
                                color=HeadingText
                            )
                            Text("Online",fontSize=11.sp,color=Secondary)
                        }
                    }
                },
                navigationIcon={
                    IconButton(onClick=onNavigateBack)
                    {
                        Icon(Icons.Rounded.ArrowBackIosNew,null,tint=HeadingText)
                    }
                },
                colors=TopAppBarDefaults.topAppBarColors(containerColor=CardSurface)
            )
        },
        bottomBar={
            ChatInputBar(
                text= inputText,
                onTextChange={inputText=it},
                selectedImage=selectedImageUri,
                onImagePick={imagepicker.launch("image/*")},
                onImageClear={selectedImageUri=null},
                onSend={
                    if(inputText.isNotBlank()||selectedImageUri!=null)
                    {
                        viewModel.sendMessage(
                            conversationId=conversationId,
                            senderId=currentUserId,
                            text=inputText.trim()
                            //should be uploaded to storage first best pratice but send like this
                        )
                        inputText=""
                        selectedImageUri=null
                    }
                }
            )
        }
        {
            padding->
                LazyColumn(
                    state = listState,
                    modifier=Modifier.fillMaxSize().padding(padding),
                    contentPadding=PaddingValues(horizontal=16.dp,vertical=12.dp),
                    verticalArrangement=Arragement.spacedBy(4.dp)
                )
                {
                    items(messages,key={it.id}){message->
                        MessageBubble(
                            message=message,
                            isMine=message.senderId==currentUserId
                        )
                    }
                }
            }
    )
}




//still todo messagebubble and chatinput bar  modify viewmodel and add routes for chat
