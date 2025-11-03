import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  StatusBar,
} from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../App';

type Props = NativeStackScreenProps<RootStackParamList, 'Home'>;

// 보고서 더미 데이터
const DUMMY_REPORTS = [
  {
    id: 'CB00000000842',
    patient: '구급환자 | 정보',
    status: 65,
    statusText: '65% 작성중',
    date: '2024-04-05',
    time: '지정시간',
    location: '강남 도로변 (구급대 11 일반)',
  },
  {
    id: 'CB00000000843',
    patient: '구급환자 | 정보',
    status: 65,
    statusText: '65% 작성중',
    date: '2024-04-05',
    time: '지정시간',
    location: '강남 도로변 (구급대 11 일반)',
  },
  {
    id: 'CB00000000844',
    patient: '구급환자 | 정보',
    status: 65,
    statusText: '65% 작성중',
    date: '2024-04-05',
    time: '지정시간',
    location: '강남 도로변 (구급대 11 일반)',
  },
  {
    id: 'CB00000000845',
    patient: '구급환자 | 정보',
    status: 65,
    statusText: '65% 작성중',
    date: '2024-04-05',
    time: '지정시간',
    location: '강남 도로변 (구급대 11 일반)',
  },
];

// 출동지령 더미 데이터
const DISPATCH_DATA = [
  {
    id: 'CB00000000000842',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
  {
    id: 'CB00000000000843',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
  {
    id: 'CB00000000000844',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
  {
    id: 'CB00000000000845',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
];

// ReportCard 컴포넌트
const ReportCard = ({ 
  item, 
  onPress 
}: { 
  item: typeof DUMMY_REPORTS[0];
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
          <Text style={styles.statusBadge}>{item.statusText}</Text>
        </View>
      </View>

      <View style={styles.progressBarContainer}>
        <View style={styles.progressBarBg}>
          <View style={[styles.progressBarFill, { width: `${item.status}%` }]} />
        </View>
      </View>

      <View style={styles.cardFooter}>
        <View style={styles.footerLeft}>
          <Text style={styles.reportLabel}>신고번호 {item.id}</Text>
        </View>
        <View style={styles.footerRight}>
          <Text style={styles.dateText}>{item.date} {item.time}</Text>
          <Text style={styles.locationText}>{item.location}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
};

// DispatchCard 컴포넌트
const DispatchCard = ({ 
  item, 
  onPress,
  isSelected 
}: { 
  item: typeof DISPATCH_DATA[0];
  onPress: () => void;
  isSelected: boolean;
}) => {
  return (
    <TouchableOpacity
      style={[
        styles.dispatchCard,
        isSelected && styles.dispatchCardSelected,
      ]}
      onPress={onPress}
      activeOpacity={0.7}
    >
      <View style={styles.cardRow}>
        <Text style={styles.dispatchId}>{item.id} {item.type}</Text>
        <Text style={styles.dispatchDate}>{item.date}</Text>
      </View>
      <Text style={styles.dispatchLocation}>{item.location}</Text>
    </TouchableOpacity>
  );
};

function Home({ navigation }: Props) {
  const [activeTab, setActiveTab] = useState('draft');
  const [selectedDispatchId, setSelectedDispatchId] = useState<string | null>(null);

  const handleCardPress = () => {
    navigation.navigate('DispatchList');
  };

  const handleDispatchPress = (item: typeof DISPATCH_DATA[0]) => {
    setSelectedDispatchId(item.id);
    navigation.navigate('DispatchDetail', {
      dispatchId: item.id,
      patientName: item.type,
      location: item.location,
      date: item.date,
    });
  };

  // 탭에 따라 렌더링할 콘텐츠
  const renderContent = () => {
    if (activeTab === 'draft') {
      // 내 보고서 탭
      return (
        <FlatList
          data={DUMMY_REPORTS}
          keyExtractor={(item, index) => `${item.id}-${index}`}
          renderItem={({ item }) => (
            <ReportCard item={item} onPress={handleCardPress} />
          )}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
        />
      );
    } else if (activeTab === 'dispatch') {
      // 출동지령 내역 탭
      return (
        <FlatList
          data={DISPATCH_DATA}
          keyExtractor={(item, index) => `${item.id}-${index}`}
          renderItem={({ item }) => (
            <DispatchCard 
              item={item} 
              onPress={() => handleDispatchPress(item)}
              isSelected={selectedDispatchId === item.id}
            />
          )}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
        />
      );
    } else {
      // 관내 보고서 검색 탭
      return (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>관내 보고서 검색 기능 준비중</Text>
        </View>
      );
    }
  };
  
  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a1a" />

      {/* 상단 헤더 */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>보고서 메인화면</Text>
      </View>

      {/* 탭 버튼들 */}
      <View style={styles.tabContainer}>
        <TouchableOpacity
          style={[styles.tabButton, activeTab === 'draft' && styles.tabButtonActive]}
          onPress={() => setActiveTab('draft')}
        >
          <Text style={[styles.tabText, activeTab === 'draft' && styles.tabTextActive]}>
            내 보고서
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.tabButton, activeTab === 'dispatch' && styles.tabButtonActive]}
          onPress={() => setActiveTab('dispatch')}
        >
          <Text style={[styles.tabText, activeTab === 'dispatch' && styles.tabTextActive]}>
            출동지령 내역
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.tabButton, activeTab === 'search' && styles.tabButtonActive]}
          onPress={() => setActiveTab('search')}
        >
          <Text style={[styles.tabText, activeTab === 'search' && styles.tabTextActive]}>
            관내 보고서 검색
          </Text>
        </TouchableOpacity>
      </View>

      {/* 탭에 따라 다른 콘텐츠 렌더링 */}
      {renderContent()}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  header: {
    paddingTop: 50,
    paddingBottom: 16,
    paddingHorizontal: 20,
    backgroundColor: '#1a1a1a',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#ffffff',
  },
  tabContainer: {
    flexDirection: 'row',
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: '#1a1a1a',
    gap: 8,
  },
  tabButton: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderRadius: 6,
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: '#4a4a4a',
    alignItems: 'center',
  },
  tabButtonActive: {
    backgroundColor: '#3b7cff',
    borderColor: '#3b7cff',
  },
  tabText: {
    fontSize: 13,
    fontWeight: '500',
    color: '#999999',
  },
  tabTextActive: {
    color: '#ffffff',
    fontWeight: '600',
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
  dateText: {
    fontSize: 11,
    color: '#999999',
    marginBottom: 2,
  },
  locationText: {
    fontSize: 11,
    color: '#999999',
  },
  // 출동지령 카드 스타일
  dispatchCard: {
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#3a3a3a',
  },
  dispatchCardSelected: {
    borderColor: '#3b7cff',
    borderWidth: 2,
  },
  cardRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  dispatchId: {
    fontSize: 13,
    color: '#ffffff',
    fontWeight: '500',
    flex: 1,
  },
  dispatchDate: {
    fontSize: 11,
    color: '#999999',
  },
  dispatchLocation: {
    fontSize: 12,
    color: '#cccccc',
    lineHeight: 18,
  },
  // 빈 화면 스타일
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

export default Home;