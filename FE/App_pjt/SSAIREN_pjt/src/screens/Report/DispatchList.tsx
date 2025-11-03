//DispatchList.tsx
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
import { RootStackParamList } from '../../App';

type Props = NativeStackScreenProps<RootStackParamList, 'DispatchList'>;

const DISPATCH_DATA = [
  {
    id: 'CB00000000000842',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
  {
    id: 'CB00000000000842',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
  {
    id: 'CB00000000000842',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
  {
    id: 'CB00000000000842',
    type: '구급출동 | 정상',
    date: '2024-04-05 지정시간',
    location: '위치 : 시흥동특례시 강남구 테헤란로 212 거리 50km',
  },
];

function DispatchList({ navigation }: Props) {
  const [selectedId, setSelectedId] = useState<string | null>(null);

  const handleItemPress = (item: typeof DISPATCH_DATA[0]) => {
    setSelectedId(item.id);
    // 상세 화면으로 이동
    navigation.navigate('DispatchDetail', {
      dispatchId: item.id,
      patientName: item.type,
      location: item.location,
      date: item.date,
    });
  };

  const renderItem = ({ item }: { item: typeof DISPATCH_DATA[0] }) => {
    const isSelected = selectedId === item.id;
    
    return (
      <TouchableOpacity
        style={[
          styles.dispatchCard,
          isSelected && styles.dispatchCardSelected,
        ]}
        onPress={() => handleItemPress(item)}
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

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a1a" />

      {/* 지령 내역 리스트 */}
      <FlatList
        data={DISPATCH_DATA}
        keyExtractor={(item, index) => `${item.id}-${index}`}
        renderItem={renderItem}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 50,
    paddingBottom: 16,
    paddingHorizontal: 20,
    backgroundColor: '#1a1a1a',
  },
  backButton: {
    marginRight: 12,
    padding: 4,
  },
  backText: {
    fontSize: 24,
    color: '#ffffff',
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
    paddingHorizontal: 8,
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
    fontSize: 12,
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
});

export default DispatchList;