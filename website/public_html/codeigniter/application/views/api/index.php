<?php 
$db_callback = ( $this->db->dbdriver == 'postgre' ? 'pg_fetch_object' : 'mysql_fetch_object' );

header('Cache-Control: no-cache, must-revalidate');
header('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
header('Content-type: application/json');
if ( empty( $posts_query ) ) {
	$status = 'error';
	$num_rows = 0;
}
else {
	$status = 'ok';
	$num_rows = $posts_query->num_rows();
}

?>
{"status":"<?php echo $status ?>","name":"PARKING","type":"FeatureCollection","count":<?php echo $num_rows ?>,"features":[<?php 
if ( !empty( $posts_query ) ): 
	$i = 1;

	$resource = $posts_query->result_id;
	while ( $row = call_user_func_array( $db_callback , array( $resource ) ) ) {
		echo '{"geometry":{"type":"Point","coordinates":[';
		echo (float) $row->lon;
		echo ',';
		echo (float) $row->lat;
		echo ']},"type":"Feature","properties":{';

		$desc_array = explode( '__SEP__' , $row->description );
		$cat_array = explode( '__SEP__' , $row->category );
		$code_array = array_map( 'intval' , explode( '__SEP__' , $row->id_panel_code ) );

		if ( sizeof( $desc_array ) != sizeof( $cat_array ) ) { continue; }

		$panels = array();

		if ( empty( $is_html_desc ) ) {
			foreach ( $desc_array as $k => $v ) {
				$panel_data = '{"desc":' . json_encode( $desc_array[ $k ] ) . ',"cat":' . json_encode( $cat_array[ $k ] );

				if ( !empty( $has_api_key ) ) {
					$panel_data .=  ',"id_c":' . json_encode( $code_array[ $k ] ) ;
				}
				$panel_data .= '}';

				$panels[] = $panel_data;
			}

			echo '"panels":['. implode ( ',' , $panels ) .']';
		}
		else {
			foreach ( $desc_array as $k => $v ) {
				if ( $cat_array[ $k ] == 'STAT-$' ) {
					$panels[] = '<li class="paid">' . $v . '</li>';
				}
				elseif ( ( stripos( $v , '\\P' ) !== FALSE ) || ( stripos( $v , 'STAT' ) !== FALSE ) ) {
					$panels[] = '<li class="prk">' . trim( str_replace( '\\P' , '' , $v ) ) . '</li>';
				}
				elseif ( ( stripos( $v , '\\A' ) !== FALSE ) || ( stripos( $v , 'ARRÊT' ) !== FALSE ) ) {
					$panels[] = '<li class="stp">' . trim( str_replace( '\\A' , '' , $v ) ) . '</li>';
				}
				else {
					$panels[] = '<li class="def">' . $v . '</li>';
				}
			}
			echo '"desc":' . json_encode( implode( '' , $panels ) );
		}
		
		echo '},"id":' . $row->poteau_id . '}'; $i++ ;

		if ( $i <= $num_rows ) {
			echo ',';
		}
	}
endif;
?>]}
