@Composable()
fun PropertyDetailContent(
    property: Property,
    paddingValues: PaddingValues
)
{
    LazyColumn(
        modiifer=Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding=PaddingValues(bottom=100.dp)
    ){
        //images
        item{
            PropertyImageGallery(imageUrls=property.imageUrls)
        }
        //title city rating
        item{
            Column(modifier=Modifier.padding(horizontal=20.dp,vertical=16.dp)
            Text
            (
                text=property.title,
                fontWeight=FontWeight.ExtraBold,
                fontSize=24.sp,
                color=HeadingText
            )
            Spacer(modifier=Modifier.height(4.dp))
            Row(verticalAlignment=Alignment.CenterVertically)
            {
                Icon( 
                    Icon.Rounded.LocationOn,
                    contnentDescription=null,
                    tint=Primario,
                    modifier=Modifier.size(16.dp)
            }
            Spacer(modifier=Modifier.width(4.dp))
            Text(
                text=property.city,
                fontSize=15.sp,
                color=CaptionLabels
            )
            if(property.rating>0)
            {
                Spacer(modifier=Modifier.width(12.dp))
                Text("*",color=Caution,fontSize=14.sp)
                Space(modifier=Modifier.width(2.dp))
                Text(
                    text="${String.format("%1.f",property.rating)} (${property.reviewsCount} recensioni)",
                    fontSize=13.sp,
                    color=SecondaryText,
                    fontWeight=FontWeight.Medium
                )
            }
        }
    }
    HorziontalDivider(color=BorderDivider,thicknes=1.dp)
    }
        item{
            Row(
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(horzontal=20.dp,vertical=16.dp),
                horizontalArrangement=Arragement.spacedBy(24.dp)
            )
            {
                InfoChip(
                    
                )
            }

}

