package com.mobile.micasaestucasa.ui.screens.chat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    currentUserId:String,
    onNavigateToChat:(String)->Unit,
    onNavigateBack:()->Unit,
    viewModel:ChatViewModel=hiltViewModel()
)
{
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit)
    {
        viewModel.loadConversations(currentUserId);
    }
    Scaffold(
        containerColor=ScreenBackgroundColor;
        topBar={
            TopAppBar(
                title={
                    Text(
                        "Messages",
                        fontWeight=FontWeight.Bold,
                        fontSize=20.sp,
                        color=HeadingText
                    )
                },
                navigationIcon={
                    IconButton(onClick=onNavigateBack)
                    {
                        Icon(Icons.Rounded.ArrowBackIosNew,null,tint=HeadingText
                    }
                },
                colors=TopAppBarDefaults.topAppBarColors(containerColor=CardSurface);
            )
       }
    ){
        padding->
            when(val state=uiState)
            {
                is ChaUiState.Loading->{
                    Box(Modifier.fillMaxSize().padding(padding), Allignment.Center)
                    {
                        CircularProgressIndicator(color=Primario)
                    }
                }
                is ChatUiState.ConversationsLoaded -> {
                    if(state.conversations.isEmpty())
                    {
                        EmptyConversationsView(Modifier.padding(padding))
                    }
                    else{
                        LazyColumn(
                            modifier=Modifier.fillMaxSize().padding(padding),
                            contentPadding=PaddingValues(vertical=8.dp)
                        )
                        {
                            items(state.conversations){ conversation->
                                ConversationItem(
                                    conversation=conversation,
                                    currentUserId=currentUserId,
                                    onClick={onNavigateToChat(conversation.id) }
                                )
                        }
                    }
                }
            }
            is ChatUiState.Error -> {
                Box(Modifier.fillMaxSize().paddin(padding),Allignment.Center)
                {
                    Text(state.message,color=ErrorColo)
                }
            }
            else -> {}
    }
}


@Composable
private fun ConversationItem)
conversation:Conversation,
currentUserId:String,
onClick: ()->Unit
)
{
    Row(modiifer=Modifier.fillMaxWidth().clickable{onClick()}.background(CardSurface).padding(horizontal=16.dp,vertical=12.dp), verticalAllignment.CenterVertically ){
        //avatar place
        Box(modifier=Modifier.size(52.dp).clip(CircleShape).background(Sfumatura),contnetAlignment=Alignment.Center)
        {
            Icon(
                Icon.Rounded.Person,
                contentDescription=null,
                tint=Primario,
                modifier=Modifier.size(28.dp)
                )
        }
        Spacer(modifier=Modifier.width(12.dp))
        Column(modifier=Modifier.weight(1f)){
            Row(
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.SpaceBetween
            ){
                Text(
                    text=if(currentUserId==conversation.hostId) "Renter" else "Owner",
                    fontWeight=FontWeight.SemiBold,
                    fontSize=15.sp,
                    color=HeadingText
                )
                Text(
                    text=formatTimeStamp(conversation.lastMessageTimestamp),
                    fontSize=11.sp,
                    color=CaptionLabels
                )
            }
            Spacer(modifier=Modifier.height(2.dp))
            Text(
                text=conversation.lastMessage.ifBlank {"No Message"},
                fontSize=13.sp,
                color=CaptionLabels,
                maxLines=1,
                overflow=TextOverflow.Ellipsis
            )
        }
        if(conversation.unreadCount>0)
        {
            Spacer(modiifer=Modifier.width(8.dp))
            Box(
                modifier=Modifier.size(20.dp).clip(CircleShape).background(Primario), contentAlignment=Alignment.Center
            ){

                Text(
                    text="${conversation.unreadcount}",
                    fontSize=10.sp,
                    color=CardSurface,
                    fontWeight=FontWeight.Bold
                )
            }
        }
    }
    HorizontalDivider(
        modifier=Modifier.padding(start=80.dp),
        color=BorderDivider,
        thickness=0.5.dp
    )
}

@Composable
private fun EmptyConversationsView(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.ChatBubbleOutline,
                null,
                tint     = BorderDivider,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No conversations yet", color = CaptionLabels, fontSize = 16.sp)
            Text(
                "Your chat's will appear here",
                color    = BorderDivider,
                fontSize = 13.sp
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String{
    if(timestamp==0L) return ""
    val sdf=SimpleDateFormat("HH:mm", Locale.ITALY)
    return sdf.format(Date(timestamp))
}


