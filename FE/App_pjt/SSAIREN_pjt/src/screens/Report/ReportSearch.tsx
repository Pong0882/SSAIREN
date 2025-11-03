// // ReportSearch(ver1).tsx
// import React, { useState } from 'react';
// import {
//   View,
//   Text,
//   StyleSheet,
//   TouchableOpacity,
//   FlatList,
//   TextInput,
// } from 'react-native';

// // 검색 결과 더미 데이터
// const SEARCH_RESULTS = [
//   {
//     id: 'CB00000000846',
//     patient: '구급환자 | 정보',
//     status: 100,
//     statusText: '작성완료',
//     date: '2024-04-05',
//     time: '14:30',
//     location: '강남 도로변 (구급대 11 일반)',
//     reporterName: '김구급',
//     teamName: '구급대 11',
//   },
//   {
//     id: 'CB00000000847',
//     patient: '구급환자 | 정보',
//     status: 100,
//     statusText: '작성완료',
//     date: '2024-04-04',
//     time: '10:15',
//     location: '서초구 서초동 123 (구급대 12 일반)',
//     reporterName: '이응급',
//     teamName: '구급대 12',
//   },
//   {
//     id: 'CB00000000848',
//     patient: '구급환자 | 정보',
//     status: 100,
//     statusText: '작성완료',
//     date: '2024-04-03',
//     time: '16:45',
//     location: '송파구 잠실동 456 (구급대 13 일반)',
//     reporterName: '박구조',
//     teamName: '구급대 13',
//   },
// ];

// // ReportCard 컴포넌트
// const ReportCard = ({ 
//   item, 
//   onPress 
// }: { 
//   item: typeof SEARCH_RESULTS[0];
//   onPress: () => void;
// }) => {
//   return (
//     <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
//       <View style={styles.cardHeader}>
//         <View style={styles.cardLeft}>
//           <Text style={styles.reportId}>{item.id}</Text>
//           <Text style={styles.patientInfo}>{item.patient}</Text>
//         </View>
//         <View style={styles.cardRight}>
//           <Text style={[
//             styles.statusBadge,
//             item.status === 100 && styles.statusBadgeComplete
//           ]}>
//             {item.statusText}
//           </Text>
//         </View>
//       </View>

//       {item.status < 100 && (
//         <View style={styles.progressBarContainer}>
//           <View style={styles.progressBarBg}>
//             <View style={[styles.progressBarFill, { width: `${item.status}%` }]} />
//           </View>
//         </View>
//       )}

//       <View style={styles.cardFooter}>
//         <View style={styles.footerLeft}>
//           <Text style={styles.reportLabel}>신고번호 {item.id}</Text>
//           <Text style={styles.teamInfo}>작성자: {item.reporterName} ({item.teamName})</Text>
//         </View>
//         <View style={styles.footerRight}>
//           <Text style={styles.dateText}>{item.date} {item.time}</Text>
//           <Text style={styles.locationText}>{item.location}</Text>
//         </View>
//       </View>
//     </TouchableOpacity>
//   );
// };

// // Props 타입 정의 (네비게이션 없이)
// interface ReportSearchContentProps {}

// function ReportSearchContent({}: ReportSearchContentProps) {
//   const [searchQuery, setSearchQuery] = useState('');
//   const [searchResults, setSearchResults] = useState(SEARCH_RESULTS);
//   const [isSearching, setIsSearching] = useState(false);

//   const handleSearch = () => {
//     setIsSearching(true);
//     // 실제로는 API 호출
//     // 여기서는 더미 데이터 필터링
//     if (searchQuery.trim() === '') {
//       setSearchResults(SEARCH_RESULTS);
//     } else {
//       const filtered = SEARCH_RESULTS.filter(item => 
//         item.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
//         item.reporterName.includes(searchQuery) ||
//         item.teamName.includes(searchQuery) ||
//         item.location.includes(searchQuery)
//       );
//       setSearchResults(filtered);
//     }
//     setTimeout(() => setIsSearching(false), 300);
//   };

//   const handleCardPress = (item: typeof SEARCH_RESULTS[0]) => {
//     // 보고서 상세 화면으로 이동 (추후 구현)
//     console.log('Report detail:', item.id);
//   };

//   return (
//     <View style={styles.container}>
//       {/* 검색 영역 */}
//       <View style={styles.searchContainer}>
//         <TextInput
//           style={styles.searchInput}
//           placeholder="신고번호, 작성자, 구급대, 위치로 검색"
//           placeholderTextColor="#666666"
//           value={searchQuery}
//           onChangeText={setSearchQuery}
//           onSubmitEditing={handleSearch}
//           returnKeyType="search"
//         />
//         <TouchableOpacity 
//           style={styles.searchButton}
//           onPress={handleSearch}
//         >
//           <Text style={styles.searchButtonText}>검색</Text>
//         </TouchableOpacity>
//       </View>

//       {/* 필터 버튼들 */}
//       <View style={styles.filterContainer}>
//         <TouchableOpacity style={styles.filterButton}>
//           <Text style={styles.filterText}>오늘</Text>
//         </TouchableOpacity>
//         <TouchableOpacity style={styles.filterButton}>
//           <Text style={styles.filterText}>최근 7일</Text>
//         </TouchableOpacity>
//         <TouchableOpacity style={styles.filterButton}>
//           <Text style={styles.filterText}>최근 30일</Text>
//         </TouchableOpacity>
//         <TouchableOpacity style={styles.filterButton}>
//           <Text style={styles.filterText}>기간 설정</Text>
//         </TouchableOpacity>
//       </View>

//       {/* 검색 결과 */}
//       {isSearching ? (
//         <View style={styles.loadingContainer}>
//           <Text style={styles.loadingText}>검색 중...</Text>
//         </View>
//       ) : searchResults.length > 0 ? (
//         <>
//           <View style={styles.resultHeader}>
//             <Text style={styles.resultCount}>
//               총 {searchResults.length}건의 보고서
//             </Text>
//           </View>
//           <FlatList
//             data={searchResults}
//             keyExtractor={(item, index) => `${item.id}-${index}`}
//             renderItem={({ item }) => (
//               <ReportCard item={item} onPress={() => handleCardPress(item)} />
//             )}
//             contentContainerStyle={styles.listContent}
//             showsVerticalScrollIndicator={false}
//           />
//         </>
//       ) : (
//         <View style={styles.emptyContainer}>
//           <Text style={styles.emptyText}>
//             {searchQuery ? '검색 결과가 없습니다' : '검색어를 입력해주세요'}
//           </Text>
//         </View>
//       )}
//     </View>
//   );
// }

// const styles = StyleSheet.create({
//   container: {
//     flex: 1,
//     backgroundColor: '#1a1a1a',
//   },
//   searchContainer: {
//     flexDirection: 'row',
//     paddingHorizontal: 16,
//     paddingVertical: 12,
//     gap: 8,
//   },
//   searchInput: {
//     flex: 1,
//     height: 44,
//     backgroundColor: '#2a2a2a',
//     borderRadius: 8,
//     paddingHorizontal: 16,
//     fontSize: 14,
//     color: '#ffffff',
//     borderWidth: 1,
//     borderColor: '#3a3a3a',
//   },
//   searchButton: {
//     backgroundColor: '#3b7cff',
//     paddingHorizontal: 20,
//     height: 44,
//     borderRadius: 8,
//     justifyContent: 'center',
//     alignItems: 'center',
//   },
//   searchButtonText: {
//     fontSize: 14,
//     fontWeight: '600',
//     color: '#ffffff',
//   },
//   filterContainer: {
//     flexDirection: 'row',
//     paddingHorizontal: 16,
//     paddingBottom: 12,
//     gap: 8,
//   },
//   filterButton: {
//     paddingHorizontal: 12,
//     paddingVertical: 6,
//     borderRadius: 6,
//     backgroundColor: '#2a2a2a',
//     borderWidth: 1,
//     borderColor: '#3a3a3a',
//   },
//   filterText: {
//     fontSize: 12,
//     color: '#999999',
//   },
//   resultHeader: {
//     paddingHorizontal: 16,
//     paddingVertical: 8,
//   },
//   resultCount: {
//     fontSize: 13,
//     color: '#999999',
//   },
//   listContent: {
//     padding: 12,
//     paddingBottom: 24,
//   },
//   card: {
//     backgroundColor: '#2a2a2a',
//     borderRadius: 10,
//     padding: 16,
//     marginBottom: 12,
//     borderWidth: 1,
//     borderColor: '#3a3a3a',
//   },
//   cardHeader: {
//     flexDirection: 'row',
//     justifyContent: 'space-between',
//     alignItems: 'flex-start',
//     marginBottom: 12,
//   },
//   cardLeft: {
//     flex: 1,
//   },
//   cardRight: {
//     alignItems: 'flex-end',
//   },
//   reportId: {
//     fontSize: 14,
//     color: '#ffffff',
//     fontWeight: '500',
//     marginBottom: 4,
//   },
//   patientInfo: {
//     fontSize: 13,
//     color: '#999999',
//   },
//   statusBadge: {
//     fontSize: 12,
//     color: '#ffffff',
//     backgroundColor: '#4a4a4a',
//     paddingHorizontal: 10,
//     paddingVertical: 4,
//     borderRadius: 4,
//   },
//   statusBadgeComplete: {
//     backgroundColor: '#28a745',
//   },
//   progressBarContainer: {
//     marginBottom: 12,
//   },
//   progressBarBg: {
//     height: 6,
//     backgroundColor: '#3a3a3a',
//     borderRadius: 3,
//     overflow: 'hidden',
//   },
//   progressBarFill: {
//     height: '100%',
//     backgroundColor: '#3b7cff',
//     borderRadius: 3,
//   },
//   cardFooter: {
//     flexDirection: 'row',
//     justifyContent: 'space-between',
//     alignItems: 'flex-start',
//   },
//   footerLeft: {
//     flex: 1,
//   },
//   footerRight: {
//     flex: 1,
//     alignItems: 'flex-end',
//   },
//   reportLabel: {
//     fontSize: 12,
//     color: '#ffffff',
//     marginBottom: 4,
//   },
//   teamInfo: {
//     fontSize: 11,
//     color: '#999999',
//   },
//   dateText: {
//     fontSize: 11,
//     color: '#999999',
//     marginBottom: 2,
//   },
//   locationText: {
//     fontSize: 11,
//     color: '#999999',
//   },
//   loadingContainer: {
//     flex: 1,
//     justifyContent: 'center',
//     alignItems: 'center',
//   },
//   loadingText: {
//     fontSize: 16,
//     color: '#999999',
//   },
//   emptyContainer: {
//     flex: 1,
//     justifyContent: 'center',
//     alignItems: 'center',
//   },
//   emptyText: {
//     fontSize: 16,
//     color: '#999999',
//   },
// });

// export default ReportSearchContent;

// ReportSearch(ver2).tsx
import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  TextInput,
} from 'react-native';

// 검색 결과 더미 데이터
const SEARCH_RESULTS = [
  {
    id: 'CB00000000846',
    patient: '구급환자 | 정보',
    status: 100,
    statusText: '작성완료',
    date: '2024-04-05',
    time: '14:30',
    location: '강남 도로변 (구급대 11 일반)',
    reporterName: '김구급',
    teamName: '구급대 11',
  },
  {
    id: 'CB00000000847',
    patient: '구급환자 | 정보',
    status: 100,
    statusText: '작성완료',
    date: '2024-04-04',
    time: '10:15',
    location: '서초구 서초동 123 (구급대 12 일반)',
    reporterName: '이응급',
    teamName: '구급대 12',
  },
  {
    id: 'CB00000000848',
    patient: '구급환자 | 정보',
    status: 100,
    statusText: '작성완료',
    date: '2024-04-03',
    time: '16:45',
    location: '송파구 잠실동 456 (구급대 13 일반)',
    reporterName: '박구조',
    teamName: '구급대 13',
  },
];

// ReportCard 컴포넌트
const ReportCard = ({ 
  item, 
  onPress 
}: { 
  item: typeof SEARCH_RESULTS[0];
  onPress: () => void;
}) => {
  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.cardHeader}>
        <View style={styles.cardLeft}>
          <Text style={styles.reportId}>{item.id}</Text>
          <Text style={styles.patientInfo}>{item.patient}</Text>
        </View>
        <View style={styles.cardRight}>
          <Text style={[
            styles.statusBadge,
            item.status === 100 && styles.statusBadgeComplete
          ]}>
            {item.statusText}
          </Text>
        </View>
      </View>

      {item.status < 100 && (
        <View style={styles.progressBarContainer}>
          <View style={styles.progressBarBg}>
            <View style={[styles.progressBarFill, { width: `${item.status}%` }]} />
          </View>
        </View>
      )}

      <View style={styles.cardFooter}>
        <View style={styles.footerLeft}>
          <Text style={styles.reportLabel}>신고번호 {item.id}</Text>
          <Text style={styles.teamInfo}>작성자: {item.reporterName} ({item.teamName})</Text>
        </View>
        <View style={styles.footerRight}>
          <Text style={styles.dateText}>{item.date} {item.time}</Text>
          <Text style={styles.locationText}>{item.location}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
};

// Props 타입 정의 (네비게이션 없이)
interface ReportSearchContentProps {}

function ReportSearchContent({}: ReportSearchContentProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(SEARCH_RESULTS);
  const [isSearching, setIsSearching] = useState(false);

  const handleSearch = () => {
    setIsSearching(true);
    // 실제로는 API 호출
    // 여기서는 더미 데이터 필터링
    if (searchQuery.trim() === '') {
      setSearchResults(SEARCH_RESULTS);
    } else {
      const filtered = SEARCH_RESULTS.filter(item => 
        item.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.reporterName.includes(searchQuery) ||
        item.teamName.includes(searchQuery) ||
        item.location.includes(searchQuery)
      );
      setSearchResults(filtered);
    }
    setTimeout(() => setIsSearching(false), 300);
  };

  const handleCardPress = (item: typeof SEARCH_RESULTS[0]) => {
    // 보고서 상세 화면으로 이동 (추후 구현)
    console.log('Report detail:', item.id);
  };

  return (
    <View style={styles.container}>
      {/* 검색 영역 */}
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="신고번호, 작성자, 구급대, 위치로 검색"
          placeholderTextColor="#666666"
          value={searchQuery}
          onChangeText={setSearchQuery}
          onSubmitEditing={handleSearch}
          returnKeyType="search"
        />
        <TouchableOpacity 
          style={styles.searchButton}
          onPress={handleSearch}
        >
          <Text style={styles.searchButtonText}>검색</Text>
        </TouchableOpacity>
      </View>

      {/* 필터 버튼들 */}
      <View style={styles.filterContainer}>
        <TouchableOpacity style={styles.filterButton}>
          <Text style={styles.filterText}>오늘</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.filterButton}>
          <Text style={styles.filterText}>최근 7일</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.filterButton}>
          <Text style={styles.filterText}>최근 30일</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.filterButton}>
          <Text style={styles.filterText}>기간 설정</Text>
        </TouchableOpacity>
      </View>

      {/* 검색 결과 */}
      {isSearching ? (
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>검색 중...</Text>
        </View>
      ) : searchResults.length > 0 ? (
        <>
          <View style={styles.resultHeader}>
            <Text style={styles.resultCount}>
              총 {searchResults.length}건의 보고서
            </Text>
          </View>
          <FlatList
            data={searchResults}
            keyExtractor={(item, index) => `${item.id}-${index}`}
            renderItem={({ item }) => (
              <ReportCard item={item} onPress={() => handleCardPress(item)} />
            )}
            contentContainerStyle={styles.listContent}
            showsVerticalScrollIndicator={false}
          />
        </>
      ) : (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>
            {searchQuery ? '검색 결과가 없습니다' : '검색어를 입력해주세요'}
          </Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  searchContainer: {
    flexDirection: 'row',
    paddingHorizontal: 40,
    paddingVertical: 32,  // 상단 여백 증가
    alignItems: 'center',
    justifyContent: 'center',  // 중앙 정렬
    gap: 8,
  },  
  searchInput: {
    width: 400,  // flex: 1 대신 고정 너비
    height: 48,   // 높이 증가
    backgroundColor: '#2a2a2a',
    borderRadius: 10,  // 더 둥글게
    paddingHorizontal: 16,
    fontSize: 15,  // 폰트 크기 증가
    color: '#ffffff',
    borderWidth: 1,
    borderColor: '#3a3a3a',
  },
  searchButton: {
    backgroundColor: '#3b7cff',
    paddingHorizontal: 24,  // 패딩 증가
    height: 48,  // 높이 증가
    borderRadius: 10,  // 더 둥글게
    justifyContent: 'center',
    alignItems: 'center',
  },
  searchButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffffff',
  },
  filterContainer: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingBottom: 12,
    gap: 8,
    justifyContent: 'center', 
    alignItems: 'center',       
  },
  filterButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
    backgroundColor: '#2a2a2a',
    borderWidth: 1,
    borderColor: '#3a3a3a',
  },
  filterText: {
    fontSize: 12,
    color: '#999999',
  },
  resultHeader: {
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  resultCount: {
    fontSize: 13,
    color: '#999999',
  },
  listContent: {
    padding: 12,
    paddingBottom: 24,
  },
  card: {
    backgroundColor: '#2a2a2a',
    borderRadius: 10,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#3a3a3a',
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  cardLeft: {
    flex: 1,
  },
  cardRight: {
    alignItems: 'flex-end',
  },
  reportId: {
    fontSize: 14,
    color: '#ffffff',
    fontWeight: '500',
    marginBottom: 4,
  },
  patientInfo: {
    fontSize: 13,
    color: '#999999',
  },
  statusBadge: {
    fontSize: 12,
    color: '#ffffff',
    backgroundColor: '#4a4a4a',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 4,
  },
  statusBadgeComplete: {
    backgroundColor: '#28a745',
  },
  progressBarContainer: {
    marginBottom: 12,
  },
  progressBarBg: {
    height: 6,
    backgroundColor: '#3a3a3a',
    borderRadius: 3,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: '#3b7cff',
    borderRadius: 3,
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  footerLeft: {
    flex: 1,
  },
  footerRight: {
    flex: 1,
    alignItems: 'flex-end',
  },
  reportLabel: {
    fontSize: 12,
    color: '#ffffff',
    marginBottom: 4,
  },
  teamInfo: {
    fontSize: 11,
    color: '#999999',
  },
  dateText: {
    fontSize: 11,
    color: '#999999',
    marginBottom: 2,
  },
  locationText: {
    fontSize: 11,
    color: '#999999',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 16,
    color: '#999999',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999999',
  },
});

export default ReportSearchContent;